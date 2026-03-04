package domain

import domain.CapturedRequest
import config.Env

class Bin(val id: Int, val binId: String, val lastUsedAt: Long) {
    def canAcceptRequest(capturedRequest: CapturedRequest): Boolean = {
        true
    }

    def isExpired(currentTime: Long): Boolean = {
        currentTime - lastUsedAt > Env.BIN_TTL_SECONDS * 1000
    }
}
