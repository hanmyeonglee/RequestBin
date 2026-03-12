package infrastructure.database

import scalikejdbc._
import io.circe.syntax._
import io.circe.parser.decode
import java.time.Instant
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
                binId, method, path, query, headers, body, remoteHost, createdAt
            ) VALUES (
                ${bin.binId}, ${capturedRequest.method}, ${capturedRequest.path},
                ${capturedRequest.query.params.asJson.noSpaces},
                ${capturedRequest.headers.entries.asJson.noSpaces},
                ${capturedRequest.body.bytes.toArray}, ${capturedRequest.remoteHost},
                ${capturedRequest.createdAt.getEpochSecond}
            )
        """.update.apply()
    }

    def read(bin: Bin, num: Int)(implicit ctx: TxContext): Seq[CapturedRequest] = {
        implicit val session: DBSession = dbSession
        sql"""
            SELECT id, method, path, query, headers, body, remoteHost, createdAt
            FROM captured_request
            WHERE binId = ${bin.binId}
            ORDER BY createdAt DESC
            LIMIT ${num}
        """.map { rs =>
            CapturedRequest(
                id         = Some(rs.long("id")),
                method     = rs.string("method"),
                path       = rs.string("path"),
                query      = Query(
                                decode[Map[String, List[String]]](rs.string("query")) match {
                                    case Right(params) => params
                                    case Left(_)       => throw new RuntimeException("Failed to decode query JSON")
                                }
                            ),
                headers    = Headers(
                                decode[Map[String, String]](rs.string("headers")) match {
                                    case Right(headers) => headers
                                    case Left(_)        => throw new RuntimeException("Failed to decode headers JSON")
                                }
                            ),
                body       = Body(ArraySeq.from(rs.bytes("body"))),
                remoteHost = rs.string("remoteHost"),
                createdAt  = Instant.ofEpochSecond(rs.long("createdAt"))
            )
        }.list.apply()
    }
}
