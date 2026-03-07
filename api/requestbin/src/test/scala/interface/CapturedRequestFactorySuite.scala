package interface

import munit.FunSuite
import jakarta.servlet._
import jakarta.servlet.http._
import java.io.{BufferedReader, PrintWriter}
import java.util.{Collections, Enumeration, Locale}
import java.security.Principal
import scala.jdk.CollectionConverters._
import scala.collection.immutable.ArraySeq

// Minimal stub covering all abstract methods of HttpServletRequest.
// Methods not used by CapturedRequestFactory throw UnsupportedOperationException.
trait StubHttpServletRequest extends HttpServletRequest {
    // --- ServletRequest ---
    def getAttribute(name: String): AnyRef                                               = throw new UnsupportedOperationException
    def getAttributeNames(): Enumeration[String]                                         = throw new UnsupportedOperationException
    def getCharacterEncoding(): String                                                   = throw new UnsupportedOperationException
    def setCharacterEncoding(env: String): Unit                                          = throw new UnsupportedOperationException
    def getContentLength(): Int                                                          = throw new UnsupportedOperationException
    def getContentLengthLong(): Long                                                     = throw new UnsupportedOperationException
    def getContentType(): String                                                         = throw new UnsupportedOperationException
    def getInputStream(): ServletInputStream                                             = throw new UnsupportedOperationException
    def getParameter(name: String): String                                               = throw new UnsupportedOperationException
    def getParameterNames(): Enumeration[String]                                         = throw new UnsupportedOperationException
    def getParameterValues(name: String): Array[String]                                  = throw new UnsupportedOperationException
    def getParameterMap(): java.util.Map[String, Array[String]]                          = throw new UnsupportedOperationException
    def getProtocol(): String                                                            = throw new UnsupportedOperationException
    def getScheme(): String                                                              = throw new UnsupportedOperationException
    def getServerName(): String                                                          = throw new UnsupportedOperationException
    def getServerPort(): Int                                                             = throw new UnsupportedOperationException
    def getReader(): BufferedReader                                                       = throw new UnsupportedOperationException
    def getRemoteAddr(): String                                                          = throw new UnsupportedOperationException
    def getRemoteHost(): String                                                          = throw new UnsupportedOperationException
    def setAttribute(name: String, o: AnyRef): Unit                                     = throw new UnsupportedOperationException
    def removeAttribute(name: String): Unit                                              = throw new UnsupportedOperationException
    def isSecure(): Boolean                                                              = throw new UnsupportedOperationException
    def getLocale(): Locale                                                              = throw new UnsupportedOperationException
    def getLocales(): Enumeration[Locale]                                                = throw new UnsupportedOperationException
    def getRequestDispatcher(path: String): RequestDispatcher                            = throw new UnsupportedOperationException
    def getLocalName(): String                                                           = throw new UnsupportedOperationException
    def getLocalAddr(): String                                                           = throw new UnsupportedOperationException
    def getLocalPort(): Int                                                              = throw new UnsupportedOperationException
    def getRemotePort(): Int                                                             = throw new UnsupportedOperationException
    def getServletContext(): ServletContext                                               = throw new UnsupportedOperationException
    def startAsync(): AsyncContext                                                        = throw new UnsupportedOperationException
    def startAsync(req: ServletRequest, res: ServletResponse): AsyncContext              = throw new UnsupportedOperationException
    def isAsyncStarted(): Boolean                                                        = throw new UnsupportedOperationException
    def isAsyncSupported(): Boolean                                                      = throw new UnsupportedOperationException
    def getAsyncContext(): AsyncContext                                                   = throw new UnsupportedOperationException
    def getDispatcherType(): DispatcherType                                              = throw new UnsupportedOperationException
    def getRequestId(): String                                                           = throw new UnsupportedOperationException
    def getProtocolRequestId(): String                                                   = throw new UnsupportedOperationException
    def getServletConnection(): ServletConnection                                        = throw new UnsupportedOperationException

    // --- HttpServletRequest ---
    def getAuthType(): String                                                            = throw new UnsupportedOperationException
    def getCookies(): Array[Cookie]                                                      = throw new UnsupportedOperationException
    def getDateHeader(name: String): Long                                                = throw new UnsupportedOperationException
    def getHeader(name: String): String                                                  = throw new UnsupportedOperationException
    def getHeaders(name: String): Enumeration[String]                                    = throw new UnsupportedOperationException
    def getHeaderNames(): Enumeration[String]                                            = throw new UnsupportedOperationException
    def getIntHeader(name: String): Int                                                  = throw new UnsupportedOperationException
    def getMethod(): String                                                              = throw new UnsupportedOperationException
    def getPathInfo(): String                                                            = throw new UnsupportedOperationException
    def getPathTranslated(): String                                                      = throw new UnsupportedOperationException
    def getContextPath(): String                                                         = throw new UnsupportedOperationException
    def getQueryString(): String                                                         = throw new UnsupportedOperationException
    def getRemoteUser(): String                                                          = throw new UnsupportedOperationException
    def isUserInRole(role: String): Boolean                                              = throw new UnsupportedOperationException
    def getUserPrincipal(): Principal                                                    = throw new UnsupportedOperationException
    def getRequestedSessionId(): String                                                  = throw new UnsupportedOperationException
    def getRequestURI(): String                                                          = throw new UnsupportedOperationException
    def getRequestURL(): StringBuffer                                                    = throw new UnsupportedOperationException
    def getServletPath(): String                                                         = throw new UnsupportedOperationException
    def getSession(create: Boolean): HttpSession                                         = throw new UnsupportedOperationException
    def getSession(): HttpSession                                                        = throw new UnsupportedOperationException
    def changeSessionId(): String                                                        = throw new UnsupportedOperationException
    def isRequestedSessionIdValid(): Boolean                                             = throw new UnsupportedOperationException
    def isRequestedSessionIdFromCookie(): Boolean                                        = throw new UnsupportedOperationException
    def isRequestedSessionIdFromURL(): Boolean                                           = throw new UnsupportedOperationException
    def authenticate(response: HttpServletResponse): Boolean                             = throw new UnsupportedOperationException
    def login(username: String, password: String): Unit                                  = throw new UnsupportedOperationException
    def logout(): Unit                                                                   = throw new UnsupportedOperationException
    def getParts(): java.util.Collection[Part]                                           = throw new UnsupportedOperationException
    def getPart(name: String): Part                                                      = throw new UnsupportedOperationException
    def upgrade[T <: HttpUpgradeHandler](handlerClass: Class[T]): T                     = throw new UnsupportedOperationException
}

class CapturedRequestFactorySuite extends FunSuite {

    // Wraps a byte array as a ServletInputStream
    private def makeInputStream(bytes: Array[Byte]): ServletInputStream = {
        val bais = new java.io.ByteArrayInputStream(bytes)
        new ServletInputStream {
            def isFinished: Boolean                             = bais.available() == 0
            def isReady: Boolean                               = true
            def setReadListener(l: ReadListener): Unit         = ()
            def read(): Int                                    = bais.read()
        }
    }

    // Builds a minimal stub request for CapturedRequestFactory
    private def makeRequest(
        method:      String            = "GET",
        uri:         String            = "/path",
        queryString: String | Null     = "key=value",
        headers:     Map[String, String] = Map("Host" -> "example.com"),
        body:        Array[Byte]       = Array.emptyByteArray,
        remoteHost:  String            = "10.0.0.1"
    ): HttpServletRequest = new StubHttpServletRequest {
        override def getMethod(): String              = method
        override def getRequestURI(): String          = uri
        override def getQueryString(): String         = queryString
        override def getHeaderNames(): Enumeration[String] =
            Collections.enumeration(headers.keys.toList.asJava)
        override def getHeader(name: String): String  = headers.getOrElse(name, null)
        override def getInputStream(): ServletInputStream = makeInputStream(body)
        override def getRemoteHost(): String          = remoteHost
    }

    // --- normal ---

    test("normal request returns Some with all fields populated") {
        val req    = makeRequest(method = "POST", uri = "/submit", queryString = "a=1&b=2",
                                 headers = Map("X-Foo" -> "bar"), body = "data".getBytes, remoteHost = "1.2.3.4")
        val result = CapturedRequestFactory.fromHttpRequest(req, 1024L)

        assert(result.isDefined)
        val r = result.get
        assertEquals(r.method,     "POST")
        assertEquals(r.path,       "/submit")
        assertEquals(r.query.params.get("a"), Some(List("1")))
        assertEquals(r.query.params.get("b"), Some(List("2")))
        assertEquals(r.headers.entries.get("X-Foo"), Some("bar"))
        assertEquals(r.body.bytes, ArraySeq.from("data".getBytes))
        assertEquals(r.remoteHost, "1.2.3.4")
        assertEquals(r.createdAt,  0L)  // set later by RequestCollector
    }

    // --- limit boundary ---

    test("body exactly at limit returns Some") {
        val limit = 10L
        val body  = Array.fill(limit.toInt)(0x41.toByte)  // 10 bytes
        val result = CapturedRequestFactory.fromHttpRequest(makeRequest(body = body), limit)
        assert(result.isDefined)
        assertEquals(result.get.body.size, 10)
    }

    test("body one byte over limit returns None") {
        val limit = 10L
        val body  = Array.fill(limit.toInt + 1)(0x41.toByte)  // 11 bytes
        val result = CapturedRequestFactory.fromHttpRequest(makeRequest(body = body), limit)
        assertEquals(result, None)
    }

    // --- edge cases ---

    test("empty body returns Some with body.size == 0") {
        val result = CapturedRequestFactory.fromHttpRequest(makeRequest(body = Array.emptyByteArray), 1024L)
        assert(result.isDefined)
        assertEquals(result.get.body.size, 0)
    }

    test("no headers returns Some with Headers(Map.empty)") {
        val req    = makeRequest(headers = Map.empty)
        val result = CapturedRequestFactory.fromHttpRequest(req, 1024L)
        assert(result.isDefined)
        assertEquals(result.get.headers.entries, Map.empty)
    }

    // --- edge case: null queryString ---

    // Servlet may return null when no query exists.
    // The factory should treat it as an empty query string.
    test("null queryString does not throw and yields empty query params") {
        val req    = makeRequest(queryString = null)
        val result = CapturedRequestFactory.fromHttpRequest(req, 1024L)

        assert(result.isDefined)
        assertEquals(result.get.query.params, Map.empty)
    }
}
