package domain.repository

import application.TxContext
import domain.entity.CapturedRequest

trait CapturedRequestRepository {
    def save(binKey: Int, capturedRequest: CapturedRequest)(implicit ctx: TxContext): Unit
    def read(binKey: Int, num: Int)(implicit ctx: TxContext): Seq[CapturedRequest]
}
