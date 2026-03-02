package interface
import org.scalatra._

class RequestBinServlet extends ScalatraServlet {
  private val baseDomain = sys.env("REQUESTBIN_BASE_DOMAIN").toLowerCase.split('.').toList

  before("/*") {
    request.getServerName.toLowerCase.split('.').toList match {
      case `baseDomain` => {}
      case binId :: `baseDomain` if binId.nonEmpty && binId.forall(_.isLetterOrDigit) => {
        // call request capture application
        // halt(200) or halt(404)
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
