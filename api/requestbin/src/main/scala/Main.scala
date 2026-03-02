import scalikejdbc.config._
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.ee10.servlet.ServletContextHandler
import org.eclipse.jetty.ee10.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

@main def main(): Unit = {
  DBs.setupAll()
  
  val port = sys.env.getOrElse("PORT", "80").toInt
  val server = new Server(port)
  val context = new ServletContextHandler()

  context.setContextPath("/")
  context.addEventListener(new ScalatraListener)
  
  server.setHandler(context)
  server.start()
  server.join()
}
