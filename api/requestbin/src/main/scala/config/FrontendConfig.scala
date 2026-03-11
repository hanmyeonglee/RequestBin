package config

// Configuration values exposed to the frontend for Microsoft Entra login flow.
case class FrontendConfig(
    tenantId:      String,
    clientId:      String,
    scope:         String
)

object FrontendConfig {
    // scope uses .default to request all permissions registered on the API app registration.
    def fromEnv(): FrontendConfig = FrontendConfig(
        tenantId      = Env.ENTRA_TENANT_ID,
        clientId      = Env.ENTRA_CLIENT_ID,
        scope         = s"${Env.ENTRA_CLIENT_ID}/.default"
    )
}
