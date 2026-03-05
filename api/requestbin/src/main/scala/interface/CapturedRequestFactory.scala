package interface

import scalikejdbc.WrappedResultSet
import domain.entity.CapturedRequest
import jakarta.servlet.http.HttpServletRequest
import scala.jdk.CollectionConverters._
import scala.collection.immutable.ArraySeq

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
            body        = ArraySeq.from(request.getInputStream.readAllBytes()),
            remoteHost  = request.getRemoteHost
        )
    }
}
