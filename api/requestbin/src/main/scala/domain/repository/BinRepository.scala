package domain.repository

import java.time.Instant
import domain.shared.TxContext
import domain.entity.Bin

trait BinRepository {
    def findByBinId(binId: String)(implicit ctx: TxContext): Option[Bin]
    def deleteAllExpiredBins(threshold: Instant)(implicit ctx: TxContext): Unit
    def save(bin: Bin)(implicit ctx: TxContext): Unit
}
