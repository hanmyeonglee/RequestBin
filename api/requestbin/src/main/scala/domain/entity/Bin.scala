package domain.entity

import java.time.{Duration, Instant}
import domain.entity.CapturedRequest

final case class Bin(val binId: String, val lastUsedAt: Instant) {
    def canAcceptRequest(
        capturedRequest: CapturedRequest,
        now: Instant,
        ttl: Duration
    ): Boolean = {
        !isExpired(now, ttl)
    }

    def isExpired(now: Instant, ttl: Duration): Boolean = {
        Duration.between(lastUsedAt, now).compareTo(ttl) > 0
    }

    def markLastUsed(now: Instant): Bin = {
        Bin(binId, now)
    }
}
