package domain

import domain.CapturedRequest

class Bin(val id: Int, val binId: String) {
    def canAcceptRequest(capturedRequest: CapturedRequest): Boolean = {
        true
    }
}
