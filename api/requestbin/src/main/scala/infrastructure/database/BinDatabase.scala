package infrastructure.database

import scalikejdbc._
import domain.entity.Bin
import application.TxContext
import domain.repository.BinRepository

class BinDatabase extends BinRepository with JdbcRepository {

    def findByBinId(binId: String)(implicit ctx: TxContext): Option[Bin] = {
        implicit val session: DBSession = dbSession
        sql"""
            SELECT id, binId, lastUsedAt
            FROM bin
            WHERE binId = ${binId}
        """
            .map(rs => Bin(rs.int("id"), rs.string("binId"), rs.long("lastUsedAt")))
            .single
            .apply()
    }

    def deleteAllExpiredBins(thresholdTime: Long)(implicit ctx: TxContext): Unit = {
        implicit val session: DBSession = dbSession
        sql"""
            DELETE FROM bin
            WHERE lastUsedAt < ${thresholdTime}
        """.update.apply()
    }

    def save(bin: Bin)(implicit ctx: TxContext): Unit = {
        implicit val session: DBSession = dbSession
        sql"""
            INSERT INTO bin (binId, lastUsedAt)
            VALUES (${bin.binId}, ${bin.lastUsedAt})
            ON CONFLICT (binId)
            DO UPDATE SET lastUsedAt = excluded.lastUsedAt
        """.update.apply()
    }
}
