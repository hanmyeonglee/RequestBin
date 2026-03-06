package config

import scalikejdbc._
import scalikejdbc.config._
import java.util.concurrent.atomic.AtomicBoolean

object InitDatabase {
    private type Migration = DBSession => Unit
    private val initialized = new AtomicBoolean(false)

    private val migrations: Seq[Migration] = Seq(
        pragmaSetting,
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

    private def pragmaSetting(session: DBSession): Unit = {
        implicit val implicitSession: DBSession = session
        sql"PRAGMA foreign_keys = ON".update.apply()
    }

    private def createBinTable(session: DBSession): Unit = {
        implicit val implicitSession: DBSession = session

        sql"""
            CREATE TABLE IF NOT EXISTS bin (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                binId TEXT NOT NULL UNIQUE,
                lastUsedAt INTEGER NOT NULL
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
                binId INTEGER NOT NULL,
                method TEXT NOT NULL,
                path TEXT NOT NULL,
                query TEXT NOT NULL,
                headers TEXT NOT NULL,
                body BLOB NOT NULL,
                remoteHost TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY (binId) REFERENCES bin(binId) ON DELETE CASCADE
            )
        """.update.apply()
        sql"""
            CREATE INDEX IF NOT EXISTS idx_captured_request_binId_createdAt_desc
            ON captured_request (binId, createdAt DESC, id DESC)
        """.update.apply()
    }
}
