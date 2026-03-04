package infrastructure

import scalikejdbc._
import domain.CapturedRequest
import infrastructure.CapturedRequestFactory

object CapturedRequestRepository {
    private implicit val session: DBSession = AutoSession

    def save(
        binKey: Int,
        method: String,
        path: String,
        query: String,
        headers: String,
        body: String,
        remoteHost: String
    ): Unit = {
        sql"""
            INSERT INTO captured_request (
                binKey, method, path, query, headers, body, remoteHost, createdAt
            ) VALUES (
                ${binKey}, ${method}, ${path}, ${query}, ${headers}, ${body}, ${remoteHost}, CURRENT_TIMESTAMP
            )
        """.update.apply()
    }

    def read(binKey: Int, num: Int): Seq[CapturedRequest] = {
        sql"""
            SELECT method, path, query, headers, body, remoteHost
            FROM captured_request
            WHERE binKey = ${binKey}
            ORDER BY createdAt DESC
            LIMIT ${num}
        """.map(CapturedRequestFactory.fromDBResult).list.apply()
    }
}
