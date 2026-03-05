package config

object Env {
    // Server Configuration
    val PORT: Int = sys.env.getOrElse("PORT", "80").toInt
    
    // Domain Configuration
    val BASE_DOMAIN: String = sys.env("REQUESTBIN_BASE_DOMAIN")

    // Resource Limits (DoS Prevention)
    // 10MB limit for request body to prevent memory exhaustion
    val MAX_CONTENT_LENGTH: Int = sys.env.getOrElse("MAX_CONTENT_LENGTH", (10 * 1024 * 1024).toString).toInt
    
    // 10KB limit for request headers
    val MAX_HEADER_SIZE: Int = sys.env.getOrElse("MAX_HEADER_SIZE", (10 * 1024).toString).toInt

    // Bin Configuration
    // Time-to-live for bins in seconds
    val BIN_TTL_SECONDS: Int = sys.env.getOrElse("SECONDS_TO_LIVE", (15 * 60).toString).toInt

    // Cleanup Scheduler Configuration
    val CLEANUP_TIME_HOUR: Int = sys.env.getOrElse("CLEANUP_TIME_HOUR", "3").toInt
    val CLEANUP_INTERVAL_SECONDS: Int = sys.env.getOrElse("CLEANUP_INTERVAL_SECONDS", (5 * 60).toString).toInt
}
