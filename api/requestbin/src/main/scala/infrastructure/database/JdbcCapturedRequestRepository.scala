package infrastructure.database

import scalikejdbc._
import domain.entity.CapturedRequest
import domain.shared.TxContext
import domain.repository.CapturedRequestRepository
import scala.collection.immutable.ArraySeq

class JdbcCapturedRequestRepository extends CapturedRequestRepository with JdbcRepository {
    def save(
        binKey: Int,
        capturedRequest: CapturedRequest
    )(implicit ctx: TxContext): Unit = {
        implicit val session: DBSession = dbSession
        sql"""
            INSERT INTO captured_request (
                binKey, method, path, query, headers, body, remoteHost
            ) VALUES (
                ${binKey}, ${capturedRequest.method}, ${capturedRequest.path},
                ${capturedRequest.query}, ${capturedRequest.headers},
                ${capturedRequest.body.toArray}, ${capturedRequest.remoteHost}
            )
        """.update.apply()
    }

    def read(binKey: Int, num: Int)(implicit ctx: TxContext): Seq[CapturedRequest] = {
        implicit val session: DBSession = dbSession
        sql"""
            SELECT method, path, query, headers, body, remoteHost
            FROM captured_request
            WHERE binKey = ${binKey}
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
