import org.scalatra.LifeCycle
import jakarta.servlet.ServletContext
import interface.RequestBinServlet

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext): Unit = {
    context.mount(new RequestBinServlet, "/*")
  }
}
