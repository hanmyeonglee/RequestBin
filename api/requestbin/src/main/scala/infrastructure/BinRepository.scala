package infrastructure

import scalikejdbc._
import domain.Bin

object BinRepository {
    private implicit val session: DBSession = AutoSession
    val secondsToLive = sys.env("SECONDS_TO_LIVE").toInt

    def fromBinId(binId: String): Option[Bin] = {
        sql"""
            SELECT id, binId
            FROM bin
            WHERE binId = ${binId} and lastUsedAt >= datetime('now', '-${secondsToLive} seconds')
        """
            .map(rs => Bin(rs.int("id"), rs.string("binId")))
            .single
            .apply()
    }

    def deleteAllExpiredBins: Unit = {
        sql"""
            DELETE FROM bin
            WHERE lastUsedAt < datetime('now', '-${secondsToLive} seconds')
        """.update.apply()
    }
}
