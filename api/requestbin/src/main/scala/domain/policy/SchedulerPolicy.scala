package domain.policy

import java.time.{Duration, Instant, ZoneId, ZonedDateTime}

final case class SchedulerPolicy(ttl: Duration, interval: Duration, cleanUpTimeHour: Int) {
    def isCertainTimeHour(now: Instant): Boolean = {
        ZonedDateTime.ofInstant(now, ZoneId.of("Asia/Seoul")).getHour == cleanUpTimeHour
    }

    def isFirstCleanTime(now: Instant, lastCleaned: Instant): Boolean = {
        Duration.between(lastCleaned, now).compareTo(Duration.ofHours(12)) > 0
    }
}
