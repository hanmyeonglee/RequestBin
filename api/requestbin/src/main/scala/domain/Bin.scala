package domain

import domain.CapturedRequest

class Bin(val id: Int, val binId: String) {
    def canAcceptRequest(capturedRequest: CapturedRequest): Boolean = {
        if (
            capturedRequest.totalSize > 10 * 1024 * 1024 ||
            capturedRequest.toMap.values.exists(_.length > 1024 * 1024)
        ) false
        else true
    }
}
