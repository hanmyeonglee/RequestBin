package application

import munit.FunSuite
import domain.entity.Bin
import domain.policy.SchedulerPolicy
import domain.repository.BinRepository
import domain.shared.{Clock, TxContext, TxManager}

class BinCleanerSuite extends FunSuite {
    private val policy = SchedulerPolicy(ttlSeconds = 900L, intervalSeconds = 300L, cleanUpTimeHour = 3)

    private val stubTx = new TxManager {
        def withTx[T](block: TxContext => T): T = block(new TxContext {})
    }

    // day 2, 3:00 AM → satisfies isCertainTimeHour(3) and 97200s >> 43200s from init(0)
    private val cleanTime    = 86400L + 10800L
    private val notCleanTime = 86400L + 14400L   // 4:00 AM

    // Mutable Clock whose time can be advanced in tests
    class MutableClock(initialTime: Long) extends Clock {
        var time: Long = initialTime
        def currentUnixTimeSeconds: Long = time
    }

    private def makeRepo(onDelete: Long => Unit = _ => ()): BinRepository = new BinRepository {
        def findByBinId(id: String)(implicit ctx: TxContext): Option[Bin] = None
        def deleteAllExpiredBins(threshold: Long)(implicit ctx: TxContext): Unit = onDelete(threshold)
        def save(bin: Bin)(implicit ctx: TxContext): Unit = ()
    }

    test("run calls deleteAllExpiredBins with correct threshold when conditions are met") {
        var deletedThreshold: Option[Long] = None
        val clock = new MutableClock(0L)

        val cleaner = new BinCleaner(stubTx, makeRepo(t => deletedThreshold = Some(t)), clock, policy)
        clock.time = cleanTime
        cleaner.run

        assertEquals(deletedThreshold, Some(cleanTime - policy.ttlSeconds))
    }

    test("run does not call deleteAllExpiredBins when not at clean hour") {
        var called = false
        val clock  = new MutableClock(0L)

        val cleaner = new BinCleaner(stubTx, makeRepo(_ => { called = true }), clock, policy)
        clock.time = notCleanTime
        cleaner.run

        assert(!called)
    }

    test("run does not call deleteAllExpiredBins when interval not met") {
        var called = false
        // init at cleanTime so lastCleaned = cleanTime; run 1s later → diff = 1 < 43200
        val clock  = new MutableClock(cleanTime)

        val cleaner = new BinCleaner(stubTx, makeRepo(_ => { called = true }), clock, policy)
        clock.time = cleanTime + 1L
        cleaner.run

        assert(!called)
    }

    test("second consecutive run is skipped after first successful clean") {
        var callCount = 0
        val clock     = new MutableClock(0L)

        val cleaner = new BinCleaner(stubTx, makeRepo(_ => { callCount += 1 }), clock, policy)
        clock.time = cleanTime
        cleaner.run
        assertEquals(callCount, 1)

        // same timestamp: lastCleaned updated to cleanTime, diff = 0 < 43200 → skipped
        cleaner.run
        assertEquals(callCount, 1)
    }
}
