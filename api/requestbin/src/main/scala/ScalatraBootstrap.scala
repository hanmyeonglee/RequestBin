import org.scalatra.LifeCycle
import jakarta.servlet.ServletContext
import interface.RequestBinServlet
import scalikejdbc.config._
import config.InitDatabase

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext): Unit = {
    context.mount(new RequestBinServlet, "/*")

    DBs.setupAll()
    InitDatabase.init()
  }
}
