package domain

import scala.annotation.static
import jakarta.servlet.http.HttpServletRequest

class CapturedRequest(
    val method: String,
    val path: String,
    val query: String,
    val fragment: String,
    val headers: String,
    val body: String,
    val remoteHost: String
) {
}
