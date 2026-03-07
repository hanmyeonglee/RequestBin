package interface

import domain.entity.{Body, CapturedRequest, Headers, Query}
import jakarta.servlet.http.HttpServletRequest
import scala.jdk.CollectionConverters._
import scala.collection.immutable.ArraySeq
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import org.scalatra.util.MapQueryString
import org.apache.commons.io.input.BoundedInputStream

object CapturedRequestFactory {
    // createdAt is set by the application layer (RequestCollector) at persist time
    def fromHttpRequest(request: HttpServletRequest, limit: Long): Option[CapturedRequest] = {
        val bodyBytes = request.getInputStream.readNBytes(limit.toInt + 1)

        if (bodyBytes.length <= limit) {
            val query = Option(request.getQueryString) match {
                case Some(q) => Query(MapQueryString.parseString(q).toMap)
                case None    => Query(Map.empty)
            }
            Some(CapturedRequest(
                method     = request.getMethod,
                path       = request.getRequestURI,
                query      = query,
                headers    = Headers(
                                request.getHeaderNames
                                        .asIterator().asScala
                                        .map(name => (name, request.getHeader(name)))
                                        .toMap
                            ),
                body       = Body(ArraySeq.from(bodyBytes)),
                remoteHost = request.getRemoteHost,
                createdAt  = 0L
            ))
        }
        else None
    }
}
