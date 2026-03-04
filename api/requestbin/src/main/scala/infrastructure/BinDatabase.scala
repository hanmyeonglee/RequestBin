package infrastructure

import scalikejdbc._
import domain.Bin
import application.{BinRepository, TxContext}

class BinDatabase extends BinRepository with JdbcRepository {
    val secondsToLive = sys.env("SECONDS_TO_LIVE").toInt

    def findByBinId(binId: String)(implicit ctx: TxContext): Option[Bin] = {
        implicit val session: DBSession = dbSession
        sql"""
            SELECT id, binId
            FROM bin
            WHERE binId = ${binId} and lastUsedAt >= datetime('now', '-${secondsToLive} seconds')
        """
            .map(rs => Bin(rs.int("id"), rs.string("binId")))
            .single
            .apply()
    }

    def updateLastUsedAt(id: Int)(implicit ctx: TxContext): Unit = {
        implicit val session: DBSession = dbSession
        sql"""
            UPDATE bin
            SET lastUsedAt = CURRENT_TIMESTAMP
            WHERE id = ${id}
        """.update.apply()
    }

    def deleteAllExpiredBins(implicit ctx: TxContext): Unit = {
        implicit val session: DBSession = dbSession
        sql"""
            DELETE FROM bin
            WHERE lastUsedAt < datetime('now', '-${secondsToLive} seconds')
        """.update.apply()
    }
}
