package application

import munit.FunSuite
import java.time.{Duration, Instant}
import domain.entity.Bin
import domain.policy.SchedulerPolicy
import domain.repository.BinRepository
import domain.shared.{Clock, TxContext, TxManager}

class BinCleanerSuite extends FunSuite {
    private val policy = SchedulerPolicy(ttl = Duration.ofSeconds(900L), interval = Duration.ofSeconds(300L), cleanUpTimeHour = 3)

    private val stubTx = new TxManager {
        def withTx[T](block: TxContext => T): T = block(new TxContext {})
    }

    // 3:00 AM KST (Asia/Seoul) = 18:00 UTC, and > 12h from Instant.EPOCH
    private val cleanTime    = Instant.ofEpochSecond(64800L)  // 18:00 UTC = 3:00 AM KST
    private val notCleanTime = Instant.ofEpochSecond(68400L)  // 19:00 UTC = 4:00 AM KST

    // Mutable Clock whose time can be advanced in tests
    class MutableClock(initialTime: Instant) extends Clock {
        var time: Instant = initialTime
        def now(): Instant = time
    }

    private def makeRepo(onDelete: Instant => Unit = _ => ()): BinRepository = new BinRepository {
        def findByBinId(id: String)(implicit ctx: TxContext): Option[Bin] = None
        def deleteAllExpiredBins(threshold: Instant)(implicit ctx: TxContext): Unit = onDelete(threshold)
        def save(bin: Bin)(implicit ctx: TxContext): Unit = ()
    }

    test("run calls deleteAllExpiredBins with correct threshold when conditions are met") {
        var deletedThreshold: Option[Instant] = None
        val clock = new MutableClock(Instant.EPOCH)

        val cleaner = new BinCleaner(stubTx, makeRepo(t => deletedThreshold = Some(t)), clock, policy)
        clock.time = cleanTime
        cleaner.run

        assertEquals(deletedThreshold, Some(cleanTime.minus(policy.ttl)))
    }

    test("run does not call deleteAllExpiredBins when not at clean hour") {
        var called = false
        val clock  = new MutableClock(Instant.EPOCH)

        val cleaner = new BinCleaner(stubTx, makeRepo(_ => { called = true }), clock, policy)
        clock.time = notCleanTime
        cleaner.run

        assert(!called)
    }

    test("run does not call deleteAllExpiredBins when interval not met") {
        var called = false
        // init at cleanTime so lastCleaned = cleanTime; run 1s later → diff = 1s < 12h
        val clock  = new MutableClock(cleanTime)

        val cleaner = new BinCleaner(stubTx, makeRepo(_ => { called = true }), clock, policy)
        clock.time = cleanTime.plusSeconds(1L)
        cleaner.run

        assert(!called)
    }

    test("second consecutive run is skipped after first successful clean") {
        var callCount = 0
        val clock     = new MutableClock(Instant.EPOCH)

        val cleaner = new BinCleaner(stubTx, makeRepo(_ => { callCount += 1 }), clock, policy)
        clock.time = cleanTime
        cleaner.run
        assertEquals(callCount, 1)

        // same timestamp: lastCleanedAt updated to cleanTime, diff = 0 < 12h → skipped
        cleaner.run
        assertEquals(callCount, 1)
    }
}
