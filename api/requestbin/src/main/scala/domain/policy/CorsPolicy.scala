package domain.policy

final case class CorsPolicy(
    allowOrigin: String,
    allowMethods: String,
    allowHeaders: String
)

object CorsPolicy {
    val AllowAll: CorsPolicy = CorsPolicy("*", "*", "*")
}
