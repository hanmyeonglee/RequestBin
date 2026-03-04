package domain.entity

import domain.entity.CapturedRequest

class Bin(val id: Int, val binId: String, val lastUsedAt: Long) {
    def canAcceptRequest(capturedRequest: CapturedRequest): Boolean = {
        true
    }

    def isExpired(currentTime: Long, ttlSeconds: Long): Boolean = {
        currentTime - lastUsedAt > ttlSeconds * 1000
    }

    def markLastUsedTime(currentTime: Long): Bin = {
        new Bin(id, binId, currentTime)
    }
}
