package interface
import org.scalatra._

import application.RequestCollector
import domain.policy.RequestPolicy

class RequestBinServlet(collector: RequestCollector, requestPolicy: RequestPolicy) extends ScalatraServlet {
    private val baseDomainParts = requestPolicy.baseDomain.split('.').toList

    before("/*") {
        if (request.getContentLength > requestPolicy.maxContentLength) {
            halt(413, "<h1>Payload Too Large</h1>")
        }

        request.getServerName.toLowerCase.split('.').toList match {
            case `baseDomainParts` => {}
            case binId :: `baseDomainParts` if binId.nonEmpty && binId.forall(_.isLetterOrDigit) => {
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
