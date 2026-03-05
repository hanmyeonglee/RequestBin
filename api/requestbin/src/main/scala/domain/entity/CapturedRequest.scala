package domain.entity

final case class CapturedRequest(
    val method: String,
    val path: String,
    val query: Query,
    val headers: Headers,
    val body: Body,
    val remoteHost: String
) {
    def totalSize: Int =
        method.length + path.length + query.size + headers.size + body.size + remoteHost.length
}
