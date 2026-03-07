package infrastructure

import munit.FunSuite
import scalikejdbc._
import java.time.Instant
import domain.entity.Bin
import domain.shared.TxContext
import infrastructure.database.{JdbcBinRepository, JdbcTxManager}

class JdbcBinRepositorySuite extends FunSuite {

    private val txManager = new JdbcTxManager
    private val binRepo   = new JdbcBinRepository

    override def beforeAll(): Unit                    = SqliteTestHelper.setupPool()
    override def afterAll(): Unit                     = SqliteTestHelper.teardownPool()
    override def beforeEach(ctx: BeforeEach): Unit    = SqliteTestHelper.clearTables()

    private def withTx[T](block: TxContext => T): T = txManager.withTx(block)

    // --- round-trip ---

    test("save and findByBinId return the saved bin") {
        withTx { implicit ctx => binRepo.save(Bin("abc123", Instant.ofEpochSecond(1000L))) }
        val found = withTx { implicit ctx => binRepo.findByBinId("abc123") }
        assertEquals(found, Some(Bin("abc123", Instant.ofEpochSecond(1000L))))
    }

    test("save upserts: second save with same binId overwrites lastUsedAt") {
        withTx { implicit ctx => binRepo.save(Bin("upsert-bin", Instant.ofEpochSecond(1000L))) }
        withTx { implicit ctx => binRepo.save(Bin("upsert-bin", Instant.ofEpochSecond(9999L))) }
        val found = withTx { implicit ctx => binRepo.findByBinId("upsert-bin") }
        assertEquals(found.map(_.lastUsedAt), Some(Instant.ofEpochSecond(9999L)))
    }

    test("findByBinId returns None for non-existent binId") {
        val result = withTx { implicit ctx => binRepo.findByBinId("ghost") }
        assertEquals(result, None)
    }

    // --- deleteAllExpiredBins ---

    test("deleteAllExpiredBins removes bins with lastUsedAt < threshold and keeps others") {
        withTx { implicit ctx =>
            binRepo.save(Bin("expired-bin", Instant.ofEpochSecond(100L)))  // 100 < 500 → deleted
            binRepo.save(Bin("active-bin",  Instant.ofEpochSecond(600L)))  // 600 >= 500 → kept
        }
        withTx { implicit ctx => binRepo.deleteAllExpiredBins(Instant.ofEpochSecond(500L)) }

        assertEquals(withTx { implicit ctx => binRepo.findByBinId("expired-bin") }, None)
        assertEquals(withTx { implicit ctx => binRepo.findByBinId("active-bin")  }, Some(Bin("active-bin", Instant.ofEpochSecond(600L))))
    }

    test("deleteAllExpiredBins threshold is exclusive: lastUsedAt == threshold is NOT deleted") {
        withTx { implicit ctx => binRepo.save(Bin("boundary-bin", Instant.ofEpochSecond(500L))) }
        withTx { implicit ctx => binRepo.deleteAllExpiredBins(Instant.ofEpochSecond(500L)) }  // condition: lastUsedAt < 500
        val found = withTx { implicit ctx => binRepo.findByBinId("boundary-bin") }
        assertEquals(found, Some(Bin("boundary-bin", Instant.ofEpochSecond(500L))))
    }

    test("deleteAllExpiredBins cascades to captured_request rows") {
        withTx { implicit ctx => binRepo.save(Bin("cascade-bin", Instant.ofEpochSecond(100L))) }
        // insert a captured_request row directly so we can check FK cascade
        DB.localTx { session =>
            implicit val s: DBSession = session
            sql"""
                INSERT INTO captured_request (binId, method, path, query, headers, body, remoteHost, createdAt)
                VALUES (${"cascade-bin"}, "GET", "/", "{}", "{}", ${Array.emptyByteArray}, "127.0.0.1", 100)
            """.update.apply()
        }

        withTx { implicit ctx => binRepo.deleteAllExpiredBins(Instant.ofEpochSecond(500L)) }

        val remaining = DB.localTx { session =>
            implicit val s: DBSession = session
            sql"SELECT COUNT(*) FROM captured_request WHERE binId = ${"cascade-bin"}"
                .map(_.int(1)).single.apply().getOrElse(0)
        }
        assertEquals(remaining, 0)
    }

    // --- failure / side-effect cases ---

    test("dbSession throws IllegalArgumentException for non-JDBC TxContext") {
        implicit val badCtx = new TxContext {}
        intercept[IllegalArgumentException] {
            binRepo.findByBinId("any")
        }
    }

    test("exception inside withTx rolls back - data is not persisted") {
        intercept[RuntimeException] {
            withTx { implicit ctx =>
                binRepo.save(Bin("rollback-bin", Instant.ofEpochSecond(1000L)))
                throw new RuntimeException("force rollback")
            }
        }
        val result = withTx { implicit ctx => binRepo.findByBinId("rollback-bin") }
        assertEquals(result, None)
    }
}
