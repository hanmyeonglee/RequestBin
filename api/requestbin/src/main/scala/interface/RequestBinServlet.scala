package interface
import org.scalatra._
import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.slf4j.LoggerFactory

import application.{BinCreator, RequestCollector, RequestReader}
import domain.entity.{Body, CapturedRequest, Headers, Query}
import domain.policy.CorsPolicy
import domain.policy.RequestPolicy
import java.util.Base64

// Circe encoders for domain types — defined here as JSON is a presentation concern
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

class RequestBinServlet(
    collector: RequestCollector,
    corsPolicy: CorsPolicy,
    requestPolicy: RequestPolicy,
    binCreator: BinCreator,
    requestReader: RequestReader
) extends ScalatraServlet {
    private val logger = LoggerFactory.getLogger(this.getClass)
    private val baseDomainParts = requestPolicy.baseDomain.split('.').toList

    error {
        case e: Throwable =>
            logger.error("Unhandled servlet error", e)
            InternalServerError("<h1>Internal Server Error</h1>")
    }

    before("/*") {
        response.setHeader("Access-Control-Allow-Origin", corsPolicy.allowOrigin)
        response.setHeader("Access-Control-Allow-Methods", corsPolicy.allowMethods)
        response.setHeader("Access-Control-Allow-Headers", corsPolicy.allowHeaders)

        if (request.getMethod.equalsIgnoreCase("OPTIONS")) {
            halt(200)
        }

        if (request.getContentLength > requestPolicy.maxContentLength) {
            halt(413, "<h1>Payload Too Large</h1>")
        }

        request.getServerName.toLowerCase.split('.').toList match {
            case `baseDomainParts` => {}
            case binId :: `baseDomainParts` if binId.nonEmpty => {
                CapturedRequestFactory.fromHttpRequest(request, requestPolicy.maxContentLength) match {
                    case Some(capturedRequest) => 
                        collector.collect(binId, capturedRequest) match {
                            case true => halt(200, request.getRemoteHost())
                            case false => halt(404, "<h1>Not Found</h1>")
                        }
                    case None => halt(400, "<h1>Bad Request</h1>")
                }
            }
            case _ => halt(404, "<h1>Not Found</h1>")
        }
    }

    options("/*") {
        halt(200)
    }

    get("/bin/create") {
        contentType = "application/json"
        Json.obj("binId" -> Json.fromString(binCreator.create())).noSpaces
    }

    get("/bin/read/:binId/:numToRead") {
        val binId = params("binId")
        params("numToRead").toIntOption.filter(_ >= 0) match {
            case None      => halt(400, "<h1>Bad Request</h1>")
            case Some(num) =>
                requestReader.read(binId, num) match {
                    case None       => halt(404, "<h1>Not Found</h1>")
                    case Some(reqs) =>
                        contentType = "application/json"
                        reqs.asJson.noSpaces
                }
        }
    }
}
