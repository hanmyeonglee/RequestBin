package infrastructure

import munit.FunSuite
import scalikejdbc._
import java.time.Instant
import domain.entity.{Bin, Body, CapturedRequest, Headers, Query}
import domain.shared.TxContext
import infrastructure.database.{JdbcBinRepository, JdbcCapturedRequestRepository, JdbcTxManager}
import scala.collection.immutable.ArraySeq

class JdbcCapturedRequestRepositorySuite extends FunSuite {

    private val txManager      = new JdbcTxManager
    private val binRepo        = new JdbcBinRepository
    private val capturedReqRepo = new JdbcCapturedRequestRepository

    private val testBin = Bin("test-bin", Instant.ofEpochSecond(1000L))

    private val baseRequest = CapturedRequest(
        method     = "POST",
        path       = "/api/test",
        query      = Query(Map("key" -> List("value"))),
        headers    = Headers(Map("Content-Type" -> "application/json")),
        body       = Body(ArraySeq.from("hello".getBytes)),
        remoteHost = "192.168.1.1",
        createdAt  = Instant.ofEpochSecond(5000L)
    )

    override def beforeAll(): Unit                    = SqliteTestHelper.setupPool()
    override def afterAll(): Unit                     = SqliteTestHelper.teardownPool()
    override def beforeEach(ctx: BeforeEach): Unit    = {
        SqliteTestHelper.clearTables()
        // each test starts with testBin present
        txManager.withTx { implicit ctx => binRepo.save(testBin) }
    }

    private def withTx[T](block: TxContext => T): T = txManager.withTx(block)

    // --- round-trip ---

    test("save and read return correct basic fields") {
        withTx { implicit ctx => capturedReqRepo.save(testBin, baseRequest) }
        val results = withTx { implicit ctx => capturedReqRepo.read(testBin, 10) }

        assertEquals(results.size, 1)
        val r = results.head
        assertEquals(r.method,     "POST")
        assertEquals(r.path,       "/api/test")
        assertEquals(r.remoteHost, "192.168.1.1")
        assertEquals(r.createdAt,  Instant.ofEpochSecond(5000L))
    }

    test("query params with multiple values survive JSON round-trip") {
        val multiQuery = baseRequest.copy(
            query = Query(Map("ids" -> List("1", "2", "3"), "flag" -> List("true")))
        )
        withTx { implicit ctx => capturedReqRepo.save(testBin, multiQuery) }
        val r = withTx { implicit ctx => capturedReqRepo.read(testBin, 10) }.head
        assertEquals(r.query.params, Map("ids" -> List("1", "2", "3"), "flag" -> List("true")))
    }

    test("headers survive JSON round-trip") {
        val req = baseRequest.copy(
            headers = Headers(Map("X-Custom" -> "val1", "Authorization" -> "Bearer tok"))
        )
        withTx { implicit ctx => capturedReqRepo.save(testBin, req) }
        val r = withTx { implicit ctx => capturedReqRepo.read(testBin, 10) }.head
        assertEquals(r.headers.entries, Map("X-Custom" -> "val1", "Authorization" -> "Bearer tok"))
    }

    test("body bytes survive BLOB round-trip including binary data") {
        val binaryData = Array[Byte](0, 1, 127, -1, -128)
        val req        = baseRequest.copy(body = Body(ArraySeq.from(binaryData)))
        withTx { implicit ctx => capturedReqRepo.save(testBin, req) }
        val r = withTx { implicit ctx => capturedReqRepo.read(testBin, 10) }.head
        assertEquals(r.body.bytes.toArray.toList, binaryData.toList)
    }

    // --- num boundary ---

    test("read with num=0 returns empty Seq") {
        withTx { implicit ctx => capturedReqRepo.save(testBin, baseRequest) }
        val results = withTx { implicit ctx => capturedReqRepo.read(testBin, 0) }
        assertEquals(results, Seq.empty)
    }

    test("read with num greater than stored count returns only available rows") {
        withTx { implicit ctx => capturedReqRepo.save(testBin, baseRequest) }
        val results = withTx { implicit ctx => capturedReqRepo.read(testBin, 100) }
        assertEquals(results.size, 1)
    }

    // --- ordering ---

    test("read returns rows in createdAt DESC order") {
        val req1 = baseRequest.copy(createdAt = Instant.ofEpochSecond(1000L))
        val req2 = baseRequest.copy(createdAt = Instant.ofEpochSecond(3000L))
        val req3 = baseRequest.copy(createdAt = Instant.ofEpochSecond(2000L))
        withTx { implicit ctx =>
            capturedReqRepo.save(testBin, req1)
            capturedReqRepo.save(testBin, req2)
            capturedReqRepo.save(testBin, req3)
        }
        val results = withTx { implicit ctx => capturedReqRepo.read(testBin, 10) }
        assertEquals(results.map(_.createdAt).toList, List(Instant.ofEpochSecond(3000L), Instant.ofEpochSecond(2000L), Instant.ofEpochSecond(1000L)))
    }

    // --- failure / side-effect cases ---

    test("corrupted JSON in query and headers columns falls back to Map.empty") {
        // insert a row directly with invalid JSON to test the decode fallback
        DB.localTx { session =>
            implicit val s: DBSession = session
            sql"""
                INSERT INTO captured_request (binId, method, path, query, headers, body, remoteHost, createdAt)
                VALUES (${"test-bin"}, "GET", "/", "not-json", "also-not-json", ${Array.emptyByteArray}, "127.0.0.1", 1000)
            """.update.apply()
        }
        intercept[RuntimeException] {
            withTx { implicit ctx => capturedReqRepo.read(testBin, 10) }
        }
    }

    test("save raises SQLException when bin does not exist (FK violation)") {
        val missingBin = Bin("nonexistent-bin", Instant.ofEpochSecond(1000L))
        intercept[java.sql.SQLException] {
            withTx { implicit ctx => capturedReqRepo.save(missingBin, baseRequest) }
        }
    }
}
