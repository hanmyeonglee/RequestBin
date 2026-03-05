package interface

import domain.policy.SchedulerPolicy
import application.BinCleaner
import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.duration._

class BinCleanupScheduler(cleaner: BinCleaner, policy: SchedulerPolicy) {
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    def start(): Unit = {
        scheduler.scheduleAtFixedRate(
            () => cleaner.run,
            policy.intervalSeconds,
            policy.intervalSeconds,
            TimeUnit.SECONDS
        )
    }

    def stop(): Unit = {
        scheduler.shutdown()
        scheduler.awaitTermination(60, TimeUnit.SECONDS)
    }
}
