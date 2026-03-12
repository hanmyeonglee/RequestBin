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
    def totalSize: Long =
        method.length.toLong + path.length.toLong + query.size.toLong + headers.size.toLong + body.size.toLong + remoteHost.length.toLong
}
