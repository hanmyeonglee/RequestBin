package infrastructure.database

import scalikejdbc._
import domain.entity.CapturedRequest
import domain.shared.TxContext
import domain.repository.CapturedRequestRepository
import scala.collection.immutable.ArraySeq
import domain.entity.Bin

class JdbcCapturedRequestRepository extends CapturedRequestRepository with JdbcRepository {
    def save(
        bin: Bin,
        capturedRequest: CapturedRequest
    )(implicit ctx: TxContext): Unit = {
        implicit val session: DBSession = dbSession
        sql"""
            INSERT INTO captured_request (
                binKey, method, path, query, headers, body, remoteHost
            ) VALUES (
                ${bin.binId}, ${capturedRequest.method}, ${capturedRequest.path},
                ${capturedRequest.query}, ${capturedRequest.headers},
                ${capturedRequest.body.toArray}, ${capturedRequest.remoteHost}
            )
        """.update.apply()
    }

    def read(bin: Bin, num: Int)(implicit ctx: TxContext): Seq[CapturedRequest] = {
        implicit val session: DBSession = dbSession
        sql"""
            SELECT method, path, query, headers, body, remoteHost
            FROM captured_request
            WHERE binKey = ${bin.binId}
            ORDER BY createdAt DESC
            LIMIT ${num}
        """.map { rs =>
            new CapturedRequest(
                method      = rs.string("method"),
                path        = rs.string("path"),
                query       = rs.string("query"),
                headers     = rs.string("headers"),
                body        = ArraySeq.from(rs.bytes("body")),
                remoteHost  = rs.string("remoteHost")
            )
        }.list.apply()
    }
}
