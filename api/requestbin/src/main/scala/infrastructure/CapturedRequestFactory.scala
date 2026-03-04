package infrastructure

import scalikejdbc.WrappedResultSet
import domain.CapturedRequest
import jakarta.servlet.http.HttpServletRequest
import scala.jdk.CollectionConverters._

object CapturedRequestFactory {
    def fromHttpRequest(implicit request: HttpServletRequest): CapturedRequest = {
        throw new NotImplementedError("Not implemented yet")
    }

    def fromDBResult(rs: WrappedResultSet): CapturedRequest = {
        new CapturedRequest(
            method = rs.string("method"),
            path = rs.string("path"),
            query = rs.string("query"),
            headers = rs.string("headers"),
            body = rs.string("body"),
            remoteHost = rs.string("remoteHost")
        )
    }
}
