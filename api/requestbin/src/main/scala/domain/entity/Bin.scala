package domain.entity

import domain.entity.CapturedRequest

final case class Bin(val id: Option[Int], val binId: String, val lastUsedAtUnixTimeSeconds: Long) {
    def canAcceptRequest(capturedRequest: CapturedRequest): Boolean = {
        true
    }

    def isExpired(currentUnixTimeSeconds: Long, ttlSeconds: Long): Boolean = {
        currentUnixTimeSeconds - lastUsedAtUnixTimeSeconds > ttlSeconds
    }

    def markLastUsedUnixTimeSeconds(currentUnixTimeSeconds: Long): Bin = {
        Bin(id, binId, currentUnixTimeSeconds)
    }
}
