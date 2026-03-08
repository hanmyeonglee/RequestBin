package infrastructure.auth

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.{JWSVerificationKeySelector, SecurityContext}
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.{DefaultJWTClaimsVerifier, DefaultJWTProcessor}
import domain.auth.TokenValidator

import java.util.{Arrays, HashSet}
import scala.util.Try

// Validates Microsoft Entra v2 access tokens (RS256, JWKS-backed)
class EntraTokenValidator(
    jwkSource: JWKSource[SecurityContext],
    tenantId:  String,
    clientId:  String
) extends TokenValidator {

    private val issuer = s"https://login.microsoftonline.com/$tenantId/v2.0"

    // Configured once at construction; DefaultJWTProcessor is thread-safe after setup.
    private val processor = {
        val p = new DefaultJWTProcessor[SecurityContext]()
        p.setJWSKeySelector(
            new JWSVerificationKeySelector[SecurityContext](JWSAlgorithm.RS256, jwkSource)
        )
        p.setJWTClaimsSetVerifier(
            new DefaultJWTClaimsVerifier[SecurityContext](
                // java.util.Set.of() is an ImmutableSet that throws NPE on .contains(null);
                // Nimbus calls contains(null) during initialization for validation,
                // so use HashSet which handles null-containment queries safely.
                new HashSet(Arrays.asList(clientId)),
                new JWTClaimsSet.Builder().issuer(issuer).build(),
                new HashSet(Arrays.asList("exp")),
                null   // no prohibited claims
            )
        )
        p
    }

    def validate(token: String): Either[String, Unit] =
        Try(processor.process(token, null))
            .toEither
            .map(_ => ())
            .left.map(_.getMessage)
}
