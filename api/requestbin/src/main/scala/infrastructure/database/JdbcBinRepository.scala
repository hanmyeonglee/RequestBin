package infrastructure.database

import scalikejdbc._
import java.time.Instant
import domain.entity.Bin
import domain.shared.TxContext
import domain.repository.BinRepository

class JdbcBinRepository extends BinRepository with JdbcRepository {

    def findByBinId(binId: String)(implicit ctx: TxContext): Option[Bin] = {
        implicit val session: DBSession = dbSession
        sql"""
            SELECT binId, lastUsedAt
            FROM bin
            WHERE binId = ${binId}
        """
            .map(rs => Bin(rs.string("binId"), Instant.ofEpochSecond(rs.long("lastUsedAt"))))
            .single
            .apply()
    }

    def deleteAllExpiredBins(threshold: Instant)(implicit ctx: TxContext): Unit = {
        implicit val session: DBSession = dbSession
        sql"""
            DELETE FROM bin
            WHERE lastUsedAt < ${threshold.getEpochSecond}
        """.update.apply()
    }

    def save(bin: Bin)(implicit ctx: TxContext): Unit = {
        implicit val session: DBSession = dbSession
        sql"""
            INSERT INTO bin (binId, lastUsedAt)
            VALUES (${bin.binId}, ${bin.lastUsedAt.getEpochSecond})
            ON CONFLICT (binId)
            DO UPDATE SET lastUsedAt = excluded.lastUsedAt
        """.update.apply()
    }
}
