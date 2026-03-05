package interface

import domain.entity.{Body, CapturedRequest, Headers, Query}
import jakarta.servlet.http.HttpServletRequest
import scala.jdk.CollectionConverters._
import scala.collection.immutable.ArraySeq
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import org.scalatra.util.MapQueryString

object CapturedRequestFactory {
    // createdAt is set by the application layer (RequestCollector) at persist time
    def fromHttpRequest(request: HttpServletRequest): CapturedRequest =
        CapturedRequest(
            method     = request.getMethod,
            path       = request.getRequestURI,
            query      = Query(MapQueryString.parseString(request.getQueryString)),
            headers    = Headers(
                            request.getHeaderNames
                                    .asIterator().asScala
                                    .map(name => (name, request.getHeader(name)))
                                    .toMap
                        ),
            body       = Body(ArraySeq.from(request.getInputStream.readAllBytes())),
            remoteHost = request.getRemoteHost,
            createdAt  = 0L
        )
}
