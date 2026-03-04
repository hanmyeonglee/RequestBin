package interface
import org.scalatra._

import application.RequestCollector
import config.Env

class RequestBinServlet(collector: RequestCollector) extends ScalatraServlet {
    before("/*") {
        if (request.getContentLength > Env.MAX_CONTENT_LENGTH) {
            halt(413, "<h1>Payload Too Large</h1>")
        }

        request.getServerName.toLowerCase.split('.').toList match {
            case Env.BASE_DOMAIN_PARTS => {}
            case binId :: Env.BASE_DOMAIN_PARTS if binId.nonEmpty && binId.forall(_.isLetterOrDigit) => {
                collector.collect(
                    binId,
                    CapturedRequestFactory.fromHttpRequest(request)
                ) match {
                    case true => halt(200, request.getRemoteHost())
                    case false => halt(404, "<h1>Not Found</h1>")
                }
            }
            case _ => halt(404, "<h1>Not Found</h1>")
        }
    }

    get("/auth") {
    }

    get("/bin/create") {
    }

    get("/bin/delete/:binId") {
    }

    get("/bin/read/:binId/:numToRead") {
    }
}
