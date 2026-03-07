package interface

import domain.policy.SchedulerPolicy
import application.BinCleaner
import java.util.concurrent.{Executors, TimeUnit}
import scala.concurrent.duration._
import org.slf4j.LoggerFactory

class BinCleanupScheduler(cleaner: BinCleaner, policy: SchedulerPolicy) {
    private val logger = LoggerFactory.getLogger(this.getClass)
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    def start(): Unit = {
        logger.info(s"Starting bin cleanup scheduler. interval=${policy.interval}")

        val cleanupTask = new Runnable {
            override def run(): Unit = {
                try {
                    cleaner.run
                } catch {
                    case e: Throwable =>
                        logger.error("Bin cleanup task failed", e)
                }
            }
        }

        scheduler.scheduleAtFixedRate(
            cleanupTask,
            policy.interval.toSeconds,
            policy.interval.toSeconds,
            TimeUnit.SECONDS
        )
    }

    def stop(): Unit = {
        logger.info("Stopping bin cleanup scheduler")
        scheduler.shutdown()
        scheduler.awaitTermination(60, TimeUnit.SECONDS)
    }
}
