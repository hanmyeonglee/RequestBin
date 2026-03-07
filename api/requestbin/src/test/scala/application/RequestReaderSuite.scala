package application

import munit.FunSuite
import java.time.Instant
import domain.entity.{Bin, Body, CapturedRequest, Headers, Query}
import domain.repository.{BinRepository, CapturedRequestRepository}
import domain.shared.{TxContext, TxManager}
import scala.collection.immutable.ArraySeq

class RequestReaderSuite extends FunSuite {
    private val binId = "testbin123"

    private val stubTx = new TxManager {
        def withTx[T](block: TxContext => T): T = block(new TxContext {})
    }

    private val testBin = Bin(binId, Instant.EPOCH)

    private def makeRequest(i: Int) = CapturedRequest(
        method     = "GET",
        path       = s"/path$i",
        query      = Query(Map.empty),
        headers    = Headers(Map.empty),
        body       = Body(ArraySeq.empty),
        remoteHost = "127.0.0.1",
        createdAt  = Instant.ofEpochSecond(i.toLong)
    )

    private def makeRepos(
        foundBin: Option[Bin],
        stored: Seq[CapturedRequest]
    ): (BinRepository, CapturedRequestRepository) = {
        val binRepo = new BinRepository {
            def findByBinId(id: String)(implicit ctx: TxContext): Option[Bin] = foundBin
            def deleteAllExpiredBins(t: Instant)(implicit ctx: TxContext): Unit = ()
            def save(bin: Bin)(implicit ctx: TxContext): Unit                 = ()
        }
        val reqRepo = new CapturedRequestRepository {
            def save(bin: Bin, req: CapturedRequest)(implicit ctx: TxContext): Unit         = ()
            def read(bin: Bin, num: Int)(implicit ctx: TxContext): Seq[CapturedRequest]     = stored.take(num)
        }
        (binRepo, reqRepo)
    }

    test("read returns Some(requests) for existing binId") {
        val requests       = Seq(makeRequest(1), makeRequest(2))
        val (binRepo, reqRepo) = makeRepos(Some(testBin), requests)
        val result         = new RequestReader(stubTx, binRepo, reqRepo).read(binId, 10)
        assertEquals(result, Some(requests))
    }

    test("read returns None for non-existent binId") {
        val (binRepo, reqRepo) = makeRepos(None, Seq.empty)
        val result             = new RequestReader(stubTx, binRepo, reqRepo).read("ghost", 10)
        assertEquals(result, None)
    }

    test("read with num=0 returns Some(empty Seq)") {
        val (binRepo, reqRepo) = makeRepos(Some(testBin), Seq(makeRequest(1)))
        val result             = new RequestReader(stubTx, binRepo, reqRepo).read(binId, 0)
        assertEquals(result, Some(Seq.empty))
    }

    test("read returns only available requests when fewer than num exist") {
        val requests           = Seq(makeRequest(1))
        val (binRepo, reqRepo) = makeRepos(Some(testBin), requests)
        val result             = new RequestReader(stubTx, binRepo, reqRepo).read(binId, 100)
        assertEquals(result, Some(requests))
    }
}
