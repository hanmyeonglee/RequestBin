package application

import munit.FunSuite
import domain.entity.{Bin, Body, CapturedRequest, Headers, Query}
import domain.policy.BinPolicy
import domain.repository.{BinRepository, CapturedRequestRepository}
import domain.shared.{Clock, TxContext, TxManager}
import scala.collection.immutable.ArraySeq

class RequestCollectorSuite extends FunSuite {
    private val now   = 10000L
    private val ttl   = 900L
    private val binId = "testbin123"

    private val stubTx = new TxManager {
        def withTx[T](block: TxContext => T): T = block(new TxContext {})
    }

    private val fixedClock = new Clock {
        def currentUnixTimeSeconds: Long = now
    }

    private val policy = BinPolicy(ttl)

    private val testRequest = CapturedRequest(
        method     = "POST",
        path       = "/",
        query      = Query(Map.empty),
        headers    = Headers(Map.empty),
        body       = Body(ArraySeq.empty),
        remoteHost = "127.0.0.1",
        createdAt  = 0L
    )

    private val activeBin  = Bin(binId, now - 100L)
    private val expiredBin = Bin(binId, now - (ttl + 100L))

    private def makeRepos(
        foundBin: Option[Bin],
        onSaveReq: CapturedRequest => Unit = _ => ()
    ): (BinRepository, CapturedRequestRepository) = {
        var lastSavedBin: Option[Bin] = None
        val binRepo = new BinRepository {
            def findByBinId(id: String)(implicit ctx: TxContext): Option[Bin] = foundBin
            def deleteAllExpiredBins(t: Long)(implicit ctx: TxContext): Unit  = ()
            def save(bin: Bin)(implicit ctx: TxContext): Unit                 = { lastSavedBin = Some(bin) }
        }
        val reqRepo = new CapturedRequestRepository {
            def save(bin: Bin, req: CapturedRequest)(implicit ctx: TxContext): Unit = onSaveReq(req)
            def read(bin: Bin, num: Int)(implicit ctx: TxContext): Seq[CapturedRequest] = Seq.empty
        }
        (binRepo, reqRepo)
    }

    test("collect returns true and saves request for active bin") {
        var saved = false
        val (binRepo, reqRepo) = makeRepos(Some(activeBin), _ => { saved = true })
        val result = new RequestCollector(stubTx, binRepo, reqRepo, fixedClock, policy).collect(binId, testRequest)
        assert(result)
        assert(saved)
    }

    test("collect sets createdAt to current clock time") {
        var savedCreatedAt: Option[Long] = None
        val (binRepo, reqRepo) = makeRepos(Some(activeBin), r => { savedCreatedAt = Some(r.createdAt) })
        new RequestCollector(stubTx, binRepo, reqRepo, fixedClock, policy).collect(binId, testRequest)
        assertEquals(savedCreatedAt, Some(now))
    }

    test("collect updates bin lastUsedAt after saving request") {
        var updatedBin: Option[Bin] = None
        val (binRepo0, reqRepo) = makeRepos(Some(activeBin))
        val binRepo = new BinRepository {
            def findByBinId(id: String)(implicit ctx: TxContext): Option[Bin] = Some(activeBin)
            def deleteAllExpiredBins(t: Long)(implicit ctx: TxContext): Unit  = ()
            def save(bin: Bin)(implicit ctx: TxContext): Unit                 = { updatedBin = Some(bin) }
        }
        new RequestCollector(stubTx, binRepo, reqRepo, fixedClock, policy).collect(binId, testRequest)
        assertEquals(updatedBin.map(_.lastUsedAtUnixTimeSeconds), Some(now))
    }

    test("collect returns false and does not save for expired bin") {
        var saved = false
        val (binRepo, reqRepo) = makeRepos(Some(expiredBin), _ => { saved = true })
        val result = new RequestCollector(stubTx, binRepo, reqRepo, fixedClock, policy).collect(binId, testRequest)
        assert(!result)
        assert(!saved)
    }

    test("collect returns false for non-existent binId") {
        var saved = false
        val (binRepo, reqRepo) = makeRepos(None, _ => { saved = true })
        val result = new RequestCollector(stubTx, binRepo, reqRepo, fixedClock, policy).collect("nonexistent", testRequest)
        assert(!result)
        assert(!saved)
    }
}
