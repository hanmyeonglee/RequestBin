package infrastructure

import scalikejdbc._
import domain.CapturedRequest
import infrastructure.CapturedRequestFactory
import application.{CapturedRequestRepository, TxContext}

class CapturedRequestDatabase extends CapturedRequestRepository {
    def save(
        binKey: Int,
        capturedRequest: CapturedRequest
    )(implicit ctx: TxContext): Unit = {
        implicit val session = ctx.asInstanceOf[JdbcTxContext].session
        sql"""
            INSERT INTO captured_request (
                binKey, method, path, query, headers, body, remoteHost
            ) VALUES (
                ${binKey}, ${capturedRequest.method}, ${capturedRequest.path}, ${capturedRequest.query}, ${capturedRequest.headers}, ${capturedRequest.body}, ${capturedRequest.remoteHost}
            )
        """.update.apply()
    }

    def read(binKey: Int, num: Int)(implicit ctx: TxContext): Seq[CapturedRequest] = {
        implicit val session = ctx.asInstanceOf[JdbcTxContext].session
        sql"""
            SELECT method, path, query, headers, body, remoteHost
            FROM captured_request
            WHERE binKey = ${binKey}
            ORDER BY createdAt DESC
            LIMIT ${num}
        """.map(CapturedRequestFactory.fromDBResult).list.apply()
    }
}
