package domain.repository

import domain.shared.TxContext
import domain.entity.{Bin, CapturedRequest}

trait CapturedRequestRepository {
    def save(bin: Bin, capturedRequest: CapturedRequest)(implicit ctx: TxContext): Unit
    def read(bin: Bin, num: Int)(implicit ctx: TxContext): Seq[CapturedRequest]
}
