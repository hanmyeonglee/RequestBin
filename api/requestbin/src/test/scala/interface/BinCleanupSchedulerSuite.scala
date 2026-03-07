package interface

import munit.FunSuite
import application.BinCleaner
import domain.entity.Bin
import domain.policy.SchedulerPolicy
import domain.repository.BinRepository
import domain.shared.{Clock, TxContext, TxManager}
import java.util.concurrent.atomic.AtomicInteger

class BinCleanupSchedulerSuite extends FunSuite {

    // BinCleanupScheduler.start() uses policy.intervalSeconds for BOTH initial delay
    // and repeat period (TimeUnit.SECONDS).  Minimum valid period is 1 second,
    // so the "at-least-one-call" test waits just over 1 second.
    private val policy = SchedulerPolicy(ttlSeconds = 900L, intervalSeconds = 1L, cleanUpTimeHour = 3)

    private val stubTx = new TxManager {
        def withTx[T](block: TxContext => T): T = block(new TxContext {})
    }
    private val stubRepo = new BinRepository {
        def findByBinId(id: String)(implicit ctx: TxContext): Option[Bin]              = None
        def deleteAllExpiredBins(t: Long)(implicit ctx: TxContext): Unit               = ()
        def save(bin: Bin)(implicit ctx: TxContext): Unit                              = ()
    }
    private val stubClock = new Clock {
        def currentUnixTimeSeconds: Long = 0L
    }

    // Creates a BinCleaner whose run() is replaced with a side-effect counter
    private def counterCleaner(counter: AtomicInteger): BinCleaner =
        new BinCleaner(stubTx, stubRepo, stubClock, policy) {
            override def run: Unit = counter.incrementAndGet()
        }

    // --- lifecycle ---

    test("start() followed by stop() completes without exception") {
        val scheduler = new BinCleanupScheduler(counterCleaner(new AtomicInteger), policy)
        scheduler.start()
        scheduler.stop()
    }

    test("stop() without start() completes without exception") {
        // The ScheduledExecutorService is created at construction time;
        // shutting it down before any task is scheduled is valid.
        val scheduler = new BinCleanupScheduler(counterCleaner(new AtomicInteger), policy)
        scheduler.stop()
    }

    test("double stop() does not throw") {
        val scheduler = new BinCleanupScheduler(counterCleaner(new AtomicInteger), policy)
        scheduler.start()
        scheduler.stop()
        scheduler.stop()  // second stop on an already-terminated executor is a no-op
    }

    // --- execution side-effect ---

    test("run() is invoked at least once after start() and a 1.5-second wait") {
        val counter   = new AtomicInteger(0)
        val scheduler = new BinCleanupScheduler(counterCleaner(counter), policy)
        scheduler.start()
        Thread.sleep(1500L)
        scheduler.stop()
        assert(counter.get() >= 1, s"expected at least 1 invocation, got ${counter.get()}")
    }
}
