package infrastructure

import scalikejdbc.WrappedResultSet
import domain.CapturedRequest
import jakarta.servlet.http.HttpServletRequest
import scala.jdk.CollectionConverters._

object CapturedRequestFactory {
    def fromHttpRequest(request: HttpServletRequest): CapturedRequest = {
        new CapturedRequest(
            method = request.getMethod,
            path = request.getRequestURI,
            query = request.getQueryString,
            headers = request.getHeaderNames.asIterator().asScala.map { name =>
                s"$name: ${request.getHeader(name)}"
            }.mkString("\n"),
            body = request.getReader.lines().toArray.mkString("\n"),
            remoteHost = request.getRemoteHost
        )
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
