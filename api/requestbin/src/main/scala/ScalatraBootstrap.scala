import org.scalatra.LifeCycle
import jakarta.servlet.ServletContext
import interface.RequestBinServlet
import scalikejdbc.config._
import config.InitDatabase
import infrastructure.database.{BinDatabase, CapturedRequestDatabase, JdbcTxManager}
import application.RequestCollector

class ScalatraBootstrap extends LifeCycle {
    override def init(context: ServletContext): Unit = {
        val txManager = new JdbcTxManager
        val binDatabase = new BinDatabase
        val capturedRequestDatabase = new CapturedRequestDatabase

        context.mount(new RequestBinServlet(
            new RequestCollector(
                txManager,
                binDatabase,
                capturedRequestDatabase
            )
        ), "/*")

        DBs.setupAll()
        InitDatabase.init()
    }
}
