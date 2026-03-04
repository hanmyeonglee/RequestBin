import org.eclipse.jetty.server.{Server, HttpConfiguration, HttpConnectionFactory, ServerConnector}
import org.eclipse.jetty.ee10.servlet.ServletContextHandler
import org.eclipse.jetty.ee10.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

@main def main(): Unit = {
    val port = sys.env.getOrElse("PORT", "80").toInt
    val server = new Server(port)

    val httpConfig = new HttpConfiguration()
    httpConfig.setRequestHeaderSize(10 * 1024)
    
    val httpFactory = new HttpConnectionFactory(httpConfig)
    val connector = new ServerConnector(server, httpFactory)
    connector.setPort(port)
    server.addConnector(connector)

    val context = new ServletContextHandler()
    context.setContextPath("/")
    context.setMaxFormContentSize(10 * 1024 * 1024)
    context.addEventListener(new ScalatraListener)
    
    server.setHandler(context)
    server.start()
    server.join()
}
