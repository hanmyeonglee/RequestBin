package domain.auth

// Validates a raw Bearer token string.
// Returns Right(()) if the token is valid, Left(reason) otherwise.
trait TokenValidator {
    def validate(token: String): Either[String, Unit]
}
