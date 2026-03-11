import org.scalatra.LifeCycle
import jakarta.servlet.ServletContext
import interface.{RequestBinServlet, BinCleanupScheduler}
import config.Env
import scalikejdbc.config._
import config.InitDatabase
import infrastructure.database.{JdbcBinRepository, JdbcCapturedRequestRepository, JdbcTxManager}
import infrastructure.auth.EntraTokenValidator
import application.{BinCreator, BinCleaner, RequestCollector, RequestReader}
import infrastructure.shared.SystemClock
import domain.policy.{BinPolicy, CorsPolicy, RequestPolicy, SchedulerPolicy, AuthPolicy}
import infrastructure.generator.BinIdGenerator
import config.FrontendConfig
import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jose.proc.SecurityContext
import java.net.URI
import java.time.Duration

class ScalatraBootstrap extends LifeCycle {
    override def init(context: ServletContext): Unit = {
        val txManager = new JdbcTxManager
        val binDatabase = new JdbcBinRepository
        val capturedRequestDatabase = new JdbcCapturedRequestRepository
        val systemClock = new SystemClock
        val binPolicy = new BinPolicy(Duration.ofSeconds(Env.BIN_TTL_SECONDS))
        val requestPolicy = new RequestPolicy(Env.MAX_CONTENT_LENGTH, Env.BASE_DOMAIN)
        val corsPolicy = CorsPolicy.AllowAll
        val binIdGenerator = new BinIdGenerator
        val authPolicy = AuthPolicy(Env.AUTH_NECESSARY)

        // JWKS endpoint for Entra v2 — RemoteJWKSet caches keys and refreshes on key rotation.
        val jwkSource = JWKSourceBuilder.create(
            URI.create(s"https://login.microsoftonline.com/${Env.ENTRA_TENANT_ID}/discovery/v2.0/keys").toURL
        )
            .build()
        val tokenValidator = new EntraTokenValidator(jwkSource, Env.ENTRA_TENANT_ID, Env.ENTRA_CLIENT_ID)
        val frontendConfig = FrontendConfig.fromEnv()

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
            new RequestReader(txManager, binDatabase, capturedRequestDatabase),
            tokenValidator,
            authPolicy,
            frontendConfig
        ), "/*")

        DBs.setupAll()
        InitDatabase.init()

        val cleanUpPolicy = new SchedulerPolicy(
            Duration.ofSeconds(Env.BIN_TTL_SECONDS),
            Duration.ofSeconds(Env.CLEANUP_INTERVAL_SECONDS),
            Env.CLEANUP_TIME_HOUR
        )
        val binCleaner = new BinCleaner(txManager, binDatabase, systemClock, cleanUpPolicy)
        val cleanupScheduler = new BinCleanupScheduler(binCleaner, cleanUpPolicy)
        cleanupScheduler.start()
    }
}
