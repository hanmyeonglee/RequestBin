package application

import domain.{Bin, CapturedRequest}

trait TxContext

trait TxManager {
   def withTx[T](block: TxContext => T): T
}

trait BinRepository {
    def findByBinId(binId: String)(implicit ctx: TxContext): Option[Bin]
    def deleteAllExpiredBins(thresholdTime: Long)(implicit ctx: TxContext): Unit
    def save(bin: Bin)(implicit ctx: TxContext): Unit
}

trait CapturedRequestRepository {
    def save(binKey: Int, capturedRequest: CapturedRequest)(implicit ctx: TxContext): Unit
    def read(binKey: Int, num: Int)(implicit ctx: TxContext): Seq[CapturedRequest]
}
