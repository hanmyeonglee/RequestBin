package infrastructure

import scalikejdbc._

// Manages a single in-memory SQLite connection pool for infrastructure tests.
// Uses maxSize=1 so every operation reuses the same connection,
// which keeps the in-memory database alive for the duration of the test suite.
object SqliteTestHelper {

    def setupPool(): Unit = {
        Class.forName("org.sqlite.JDBC")
        ConnectionPool.singleton(
            "jdbc:sqlite::memory:",
            "",
            "",
            ConnectionPoolSettings(initialSize = 1, maxSize = 1, validationQuery = "select 1")
        )
        // PRAGMA foreign_keys must be set OUTSIDE a transaction (no-op inside one in SQLite).
        DB.autoCommit { session =>
            implicit val s: DBSession = session
            sql"PRAGMA foreign_keys = ON".update.apply()
        }
        DB.localTx { session =>
            implicit val s: DBSession = session
            sql"""
                CREATE TABLE IF NOT EXISTS bin (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    binId      TEXT    NOT NULL UNIQUE,
                    lastUsedAt INTEGER NOT NULL
                )
            """.update.apply()
            sql"CREATE INDEX IF NOT EXISTS idx_bin_binId ON bin (binId)".update.apply()
            sql"""
                CREATE TABLE IF NOT EXISTS captured_request (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    binId      TEXT    NOT NULL,
                    method     TEXT    NOT NULL,
                    path       TEXT    NOT NULL,
                    query      TEXT    NOT NULL,
                    headers    TEXT    NOT NULL,
                    body       BLOB    NOT NULL,
                    remoteHost TEXT    NOT NULL,
                    createdAt  INTEGER NOT NULL,
                    FOREIGN KEY (binId) REFERENCES bin(binId) ON DELETE CASCADE
                )
            """.update.apply()
            sql"""
                CREATE INDEX IF NOT EXISTS idx_captured_request_binId_createdAt_desc
                ON captured_request (binId, createdAt DESC, id DESC)
            """.update.apply()
        }
    }

    def teardownPool(): Unit = ConnectionPool.closeAll()

    def clearTables(): Unit = {
        // PRAGMA must be outside a transaction in SQLite.
        DB.autoCommit { session =>
            implicit val s: DBSession = session
            sql"PRAGMA foreign_keys = ON".update.apply()
        }
        DB.localTx { session =>
            implicit val s: DBSession = session
            sql"DELETE FROM captured_request".update.apply()
            sql"DELETE FROM bin".update.apply()
        }
    }
}
