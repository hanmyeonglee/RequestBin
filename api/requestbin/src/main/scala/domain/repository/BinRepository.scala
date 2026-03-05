package domain.repository

import domain.shared.TxContext
import domain.entity.{Bin, CapturedRequest}

trait BinRepository {
    def findByBinId(binId: String)(implicit ctx: TxContext): Option[Bin]
    def deleteAllExpiredBins(thresholdUnixTimeSeconds: Long)(implicit ctx: TxContext): Unit
    def save(bin: Bin)(implicit ctx: TxContext): Unit
}
