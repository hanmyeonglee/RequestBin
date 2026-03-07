import org.scalatra.LifeCycle
import jakarta.servlet.ServletContext
import interface.{RequestBinServlet, BinCleanupScheduler}
import config.Env
import scalikejdbc.config._
import config.InitDatabase
import infrastructure.database.{JdbcBinRepository, JdbcCapturedRequestRepository, JdbcTxManager}
import application.{BinCreator, BinCleaner, RequestCollector, RequestReader}
import infrastructure.shared.SystemClock
import domain.policy.{BinPolicy, CorsPolicy, RequestPolicy, SchedulerPolicy}
import infrastructure.generator.BinIdGenerator

class ScalatraBootstrap extends LifeCycle {
    override def init(context: ServletContext): Unit = {
        val txManager = new JdbcTxManager
        val binDatabase = new JdbcBinRepository
        val capturedRequestDatabase = new JdbcCapturedRequestRepository
        val systemClock = new SystemClock
        val binPolicy = new BinPolicy(Env.BIN_TTL_SECONDS)
        val requestPolicy = new RequestPolicy(Env.MAX_CONTENT_LENGTH, Env.BASE_DOMAIN)
        val corsPolicy = CorsPolicy.AllowAll
        val binIdGenerator = new BinIdGenerator

        context.mount(new RequestBinServlet(
            new RequestCollector(
                txManager,
                binDatabase,
                capturedRequestDatabase,
                systemClock,
                binPolicy
            ),
            corsPolicy,
            requestPolicy,
            new BinCreator(txManager, binDatabase, systemClock, binIdGenerator),
            new RequestReader(txManager, binDatabase, capturedRequestDatabase)
        ), "/*")

        DBs.setupAll()
        InitDatabase.init()

        val cleanUpPolicy = new SchedulerPolicy(Env.BIN_TTL_SECONDS, Env.CLEANUP_INTERVAL_SECONDS, Env.CLEANUP_TIME_HOUR)
        val binCleaner = new BinCleaner(txManager, binDatabase, systemClock, cleanUpPolicy)
        val cleanupScheduler = new BinCleanupScheduler(binCleaner, cleanUpPolicy)
        cleanupScheduler.start()
    }
}
