package infrastructure

import scalikejdbc.WrappedResultSet
import domain.CapturedRequest
import jakarta.servlet.http.HttpServletRequest
import scala.jdk.CollectionConverters._
import org.apache.commons.io.IOUtils

object CapturedRequestFactory {
    def fromHttpRequest(request: HttpServletRequest): CapturedRequest = {
        new CapturedRequest(
            method      = request.getMethod,
            path        = request.getRequestURI,
            query       = request.getQueryString,
            headers     = request.getHeaderNames
                                .asIterator()
                                .asScala
                                .map { name =>
                                    s"$name: ${request.getHeader(name)}"
                                }.mkString("\n"),
            body        = IOUtils.toByteArray(request.getInputStream),
            remoteHost  = request.getRemoteHost
        )
    }

    def fromDBResult(rs: WrappedResultSet): CapturedRequest = {
        new CapturedRequest(
            method      = rs.string("method"),
            path        = rs.string("path"),
            query       = rs.string("query"),
            headers     = rs.string("headers"),
            body        = rs.bytes("body"),
            remoteHost  = rs.string("remoteHost")
        )
    }
}
