package interface
import org.scalatra._
import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.slf4j.LoggerFactory

import application.{BinCreator, RequestCollector, RequestReader}
import config.FrontendConfig
import domain.auth.TokenValidator
import domain.entity.{Body, CapturedRequest, Headers, Query}
import domain.policy.CorsPolicy
import domain.policy.RequestPolicy
import domain.policy.AuthPolicy
import java.time.Instant
import java.util.Base64

// Circe encoders for domain types — defined here as JSON is a presentation concern
private given Encoder[Instant] =
    Encoder.instance(i => Json.fromLong(i.getEpochSecond))

private given Encoder[Body] =
    Encoder.instance(b => Json.fromString(Base64.getEncoder.encodeToString(b.bytes.toArray)))

private given Encoder[Query] =
    Encoder.instance(q => q.params.map { case (k, vs) => k -> vs.asJson }.asJson)

private given Encoder[Headers] =
    Encoder.instance(h => h.entries.map { case (k, v) => k -> v.asJson }.asJson)

private given Encoder[CapturedRequest] = Encoder.instance { r =>
    Json.obj(
        "method"     -> r.method.asJson,
        "path"       -> r.path.asJson,
        "query"      -> r.query.asJson,
        "headers"    -> r.headers.asJson,
        "body"       -> r.body.asJson,
        "remoteHost" -> r.remoteHost.asJson,
        "createdAt"  -> r.createdAt.asJson
    )
}

private given Encoder[FrontendConfig] = Encoder.instance { c =>
    Json.obj(
        "tenantId"      -> c.tenantId.asJson,
        "clientId"      -> c.clientId.asJson,
        "scope"         -> c.scope.asJson
    )
}

class RequestBinServlet(
    collector: RequestCollector,
    corsPolicy: CorsPolicy,
    requestPolicy: RequestPolicy,
    binCreator: BinCreator,
    requestReader: RequestReader,
    tokenValidator: TokenValidator,
    authPolicy: AuthPolicy,
    frontendConfig: FrontendConfig
) extends ScalatraServlet {
    private val logger = LoggerFactory.getLogger(this.getClass)
    private val baseDomainParts = requestPolicy.baseDomain.split('.').toList
    
    private val PAYLOAD_413 = "<h1>Payload Too Large</h1>"
    private val NOT_FOUND_404 = "<h1>Not Found</h1>"
    private val BAD_REQUEST_400 = "<h1>Bad Request</h1>"
    private val INTERNAL_SERVER_ERROR_500 = "<h1>Internal Server Error</h1>"
    private val UNAUTHORIZED_401 = Map("error" -> "Unauthorized").asJson.noSpaces

    private val CONTENT_TYPE_HTML = Map("Content-Type" -> "text/html; charset=UTF-8")
    private val CONTENT_TYPE_JSON = Map("Content-Type" -> "application/json; charset=UTF-8")

    private val CONFIG_JSON = frontendConfig.asJson.noSpaces

    error {
        case e: Throwable =>
            logger.error("Unhandled servlet error", e)
            InternalServerError(INTERNAL_SERVER_ERROR_500)
    }

    before("/*") {
        response.setHeader("Access-Control-Allow-Origin", corsPolicy.allowOrigin)
        response.setHeader("Access-Control-Allow-Methods", corsPolicy.allowMethods)
        response.setHeader("Access-Control-Allow-Headers", corsPolicy.allowHeaders)

        if (request.getMethod.equalsIgnoreCase("OPTIONS")) {
            halt(200)
        }

        if (request.getContentLength > requestPolicy.maxContentLength) {
            halt(413, PAYLOAD_413, CONTENT_TYPE_HTML)
        }

        request.getServerName.toLowerCase.split('.').toList match {
            case `baseDomainParts` => {}
            case binId :: `baseDomainParts` if binId.nonEmpty => {
                CapturedRequestFactory.fromHttpRequest(request, requestPolicy.maxContentLength) match {
                    case Some(capturedRequest) => 
                        collector.collect(binId, capturedRequest) match {
                            case true => halt(200, Option(request.getHeader("X-Real-IP")).getOrElse(request.getRemoteAddr))
                            case false => halt(404, NOT_FOUND_404, CONTENT_TYPE_HTML)
                        }
                    case None => halt(400, BAD_REQUEST_400, CONTENT_TYPE_HTML)
                }
            }
            case _ => halt(404, NOT_FOUND_404, CONTENT_TYPE_HTML)
        }
    }

    // Extracts the Bearer token from the Authorization header and validates it.
    // Halts with 401 if the header is missing, malformed, or the token is invalid.
    private def requireAuth(): Unit = {
        if (!authPolicy.isAuthNeeded) return
        
        val token = Option(request.getHeader("Authorization")) match {
            case Some(s"Bearer $token") => Some(token)
            case _ => None
        }
        token match {
            case None    => halt(401, UNAUTHORIZED_401, CONTENT_TYPE_JSON)
            case Some(t) => tokenValidator.validate(t).left.foreach(_ => halt(401, UNAUTHORIZED_401, CONTENT_TYPE_JSON))
        }
    }

    before("/bin/create")   { requireAuth() }
    before("/bin/read/*")   { requireAuth() }

    options("/*") {
        halt(200)
    }

    get("/config") {
        contentType = "application/json"
        CONFIG_JSON
    }

    get("/bin/create") {
        contentType = "application/json"
        Json.obj("binId" -> Json.fromString(binCreator.create())).noSpaces
    }

    get("/bin/read/:binId/:numToRead") {
        val binId = params("binId")
        params("numToRead").toIntOption.filter(_ >= 0) match {
            case None      => halt(400, BAD_REQUEST_400, CONTENT_TYPE_HTML)
            case Some(num) =>
                requestReader.read(binId, num) match {
                    case None       => halt(404, NOT_FOUND_404, CONTENT_TYPE_HTML)
                    case Some(reqs) =>
                        contentType = "application/json"
                        reqs.asJson.noSpaces
                }
        }
    }
}
