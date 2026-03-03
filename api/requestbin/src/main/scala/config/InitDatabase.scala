package config

import scalikejdbc._
import scalikejdbc.config._
import java.util.concurrent.atomic.AtomicBoolean

object InitDatabase {
  private type Migration = DBSession => Unit
  private val initialized = new AtomicBoolean(false)

  private val migrations: Seq[Migration] = Seq(
    createBinTable,
    createCapturedRequestTable
  )

  def init(): Unit = {
    if (initialized.compareAndSet(false, true)) {
      DB.localTx { session =>
        migrations.foreach(migration => migration(session))
      }
    }
  }

  private def createBinTable(session: DBSession): Unit = {
    implicit val implicitSession: DBSession = session

    sql"""
        CREATE TABLE IF NOT EXISTS bin (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            binId CHAR(10) NOT NULL,
            lastUsedAt TIMESTAMP NOT NULL
        )
    """.update.apply()
    sql"""
        CREATE INDEX IF NOT EXISTS idx_bin_binId ON bin (binId)
    """.update.apply()
  }

  private def createCapturedRequestTable(session: DBSession): Unit = {
    implicit val implicitSession: DBSession = session

    sql"""
        CREATE TABLE IF NOT EXISTS captured_request (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            binKey INTEGER NOT NULL,
            method VARCHAR(10) NOT NULL,
            path TEXT NOT NULL,
            query TEXT NOT NULL,
            fragment TEXT NOT NULL,
            headers TEXT NOT NULL,
            body TEXT NOT NULL,
            remoteHost VARCHAR(255) NOT NULL,
            createdAt TIMESTAMP NOT NULL,
            FOREIGN KEY (binKey) REFERENCES bin(id) ON DELETE CASCADE
        )
    """.update.apply()
    sql"""
        CREATE INDEX IF NOT EXISTS idx_captured_request_binKey_createdAt_desc
        ON captured_request (binKey, createdAt DESC)
    """.update.apply()
  }
}
