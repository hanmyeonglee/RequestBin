package domain.entity

import java.time.Instant

final case class CapturedRequest(
    val method: String,
    val path: String,
    val query: Query,
    val headers: Headers,
    val body: Body,
    val remoteHost: String,
    val createdAt: Instant
) {
    def totalSize: Int =
        method.length + path.length + query.size + headers.size + body.size + remoteHost.length
}
