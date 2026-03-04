import org.scalatra.LifeCycle
import jakarta.servlet.ServletContext
import interface.RequestBinServlet
import config.Env
import scalikejdbc.config._
import config.InitDatabase
import infrastructure.database.{JdbcBinRepository, JdbcCapturedRequestRepository, JdbcTxManager}
import application.RequestCollector
import infrastructure.shared.SystemClock
import domain.policy.{BinPolicy, RequestPolicy}

class ScalatraBootstrap extends LifeCycle {
    override def init(context: ServletContext): Unit = {
        val txManager = new JdbcTxManager
        val binDatabase = new JdbcBinRepository
        val capturedRequestDatabase = new JdbcCapturedRequestRepository
        val systemClock = new SystemClock
        val binPolicy = new BinPolicy(Env.BIN_TTL_SECONDS)
        val requestPolicy = new RequestPolicy(Env.MAX_CONTENT_LENGTH, Env.BASE_DOMAIN)

        context.mount(new RequestBinServlet(
            new RequestCollector(
                txManager,
                binDatabase,
                capturedRequestDatabase,
                systemClock,
                binPolicy
            ),
            requestPolicy
        ), "/*")

        DBs.setupAll()
        InitDatabase.init()
    }
}
