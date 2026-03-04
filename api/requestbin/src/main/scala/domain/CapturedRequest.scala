package domain

import scala.annotation.static

class CapturedRequest(
    val method: String,
    val path: String,
    val query: String,
    val headers: String,
    val body: String,
    val remoteHost: String
) {
    def totalSize: Int = {
        method.length + path.length + query.length + headers.length + body.length + remoteHost.length
    }

    def toMap: Map[String, String] = {
        Map(
            "method" -> method,
            "path" -> path,
            "query" -> query,
            "headers" -> headers,
            "body" -> body,
            "remoteHost" -> remoteHost
        )
    }
}
