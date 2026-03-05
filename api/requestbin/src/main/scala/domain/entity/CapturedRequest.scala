package domain.entity

import scala.annotation.static
import scala.collection.immutable.ArraySeq

final case class CapturedRequest(
    val method: String,
    val path: String,
    val query: String,
    val headers: String,
    val body: ArraySeq[Byte],
    val remoteHost: String
) {
    def totalSize: Int = {
        method.length + path.length + query.length + headers.length + body.length + remoteHost.length
    }
}
