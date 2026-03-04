package interface

import scalikejdbc.WrappedResultSet
import domain.entity.CapturedRequest
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
}
