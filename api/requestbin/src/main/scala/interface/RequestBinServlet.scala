package interface
import org.scalatra._

import application.RequestCollector
import infrastructure.CapturedRequestFactory

class RequestBinServlet(collector: RequestCollector) extends ScalatraServlet {
    private val baseDomain = sys.env("REQUESTBIN_BASE_DOMAIN").toLowerCase.split('.').toList

    before("/*") {
        request.getServerName.toLowerCase.split('.').toList match {
            case `baseDomain` => {}
            case binId :: `baseDomain` if binId.nonEmpty && binId.forall(_.isLetterOrDigit) => {
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
