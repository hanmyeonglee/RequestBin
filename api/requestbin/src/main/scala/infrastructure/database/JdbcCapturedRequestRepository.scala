package infrastructure.database

import scalikejdbc._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser.decode
import domain.entity.{Bin, Body, CapturedRequest, Headers, Query}
import domain.shared.TxContext
import domain.repository.CapturedRequestRepository
import scala.collection.immutable.ArraySeq

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
                ${capturedRequest.query.params.asJson.noSpaces},
                ${capturedRequest.headers.entries.asJson.noSpaces},
                ${capturedRequest.body.bytes.toArray}, ${capturedRequest.remoteHost}
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
            CapturedRequest(
                method     = rs.string("method"),
                path       = rs.string("path"),
                query      = Query(decode[Map[String, List[String]]](rs.string("query")).getOrElse(Map.empty)),
                headers    = Headers(decode[Map[String, String]](rs.string("headers")).getOrElse(Map.empty)),
                body       = Body(ArraySeq.from(rs.bytes("body"))),
                remoteHost = rs.string("remoteHost")
            )
        }.list.apply()
    }
}
