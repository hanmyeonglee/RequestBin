import org.eclipse.jetty.server.{Server, HttpConfiguration, HttpConnectionFactory, ServerConnector}
import org.eclipse.jetty.ee10.servlet.ServletContextHandler
import org.eclipse.jetty.ee10.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener
import config.Env

@main def main(): Unit = {
    val server = new Server()

    val httpConfig = new HttpConfiguration()
    httpConfig.setRequestHeaderSize(Env.MAX_HEADER_SIZE)
    
    val httpFactory = new HttpConnectionFactory(httpConfig)
    val connector = new ServerConnector(server, httpFactory)
    connector.setPort(Env.PORT)
    server.addConnector(connector)

    val context = new ServletContextHandler()
    context.setContextPath("/")

    context.setMaxFormContentSize(Env.MAX_CONTENT_LENGTH)
    context.addEventListener(new ScalatraListener)
    
    server.setHandler(context)
    server.start()
    server.join()
}
