package domain.policy

final case class SchedulerPolicy(ttlSeconds: Long, intervalSeconds: Long, cleanUpTimeHour: Int) {
    def isCertainTimeHour(currentUnixTimeSeconds: Long): Boolean = {
        val currentHour = currentUnixTimeSeconds % 86400 / 3600
        currentHour == cleanUpTimeHour
    }

    def isFirstCleanTime(currentUnixTimeSeconds: Long, lastCleanedUnixTimeSeconds: Long): Boolean = {
        (currentUnixTimeSeconds - lastCleanedUnixTimeSeconds) > 43200
    }
}
