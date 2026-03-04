package domain

import domain.CapturedRequest
import config.Env

class Bin(val id: Int, val binId: String) {
    def canAcceptRequest(capturedRequest: CapturedRequest): Boolean = {
        true
    }
}
