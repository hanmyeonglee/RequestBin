package infrastructure

import munit.FunSuite
import com.nimbusds.jose.{JWSAlgorithm, JWSHeader}
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.{JWKSet, RSAKey}
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
import infrastructure.auth.EntraTokenValidator

import java.util.Date

class EntraTokenValidatorSuite extends FunSuite {

    private val tenantId = "aaaabbbb-cccc-dddd-eeee-ffffffffffff"
    private val clientId = "11112222-3333-4444-5555-666677778888"
    private val issuer   = s"https://login.microsoftonline.com/$tenantId/v2.0"

    // The valid signing key known to the validator
    private val rsaKey: RSAKey   = new RSAKeyGenerator(2048).keyID("key-1").generate()
    // An unknown key — not present in the JWKS (simulates a foreign / rotated key)
    private val otherKey: RSAKey = new RSAKeyGenerator(2048).keyID("key-2").generate()

    private val validator = new EntraTokenValidator(
        new ImmutableJWKSet[SecurityContext](new JWKSet(rsaKey)),
        tenantId,
        clientId
    )

    // Builds a signed JWT with customisable claims and signing key.
    // expOffsetMs: milliseconds added to now() for the expiration time.
    private def buildToken(
        iss:        String = issuer,
        aud:        String = clientId,
        expOffsetMs: Long  = 3_600_000L,
        signingKey: RSAKey = rsaKey
    ): String = {
        val now = System.currentTimeMillis()
        val claims = new JWTClaimsSet.Builder()
            .issuer(iss)
            .audience(aud)
            .subject("test-subject")
            .issueTime(new Date(now))
            .expirationTime(new Date(now + expOffsetMs))
            .build()
        val header = new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(signingKey.getKeyID).build()
        val jwt    = new SignedJWT(header, claims)
        jwt.sign(new RSASSASigner(signingKey))
        jwt.serialize()
    }

    test("valid token passes") {
        assertEquals(validator.validate(buildToken()), Right(()))
    }

    // DefaultJWTClaimsVerifier allows clock skew of 60 s; use 120 s in the past to exceed it.
    test("expired token is rejected") {
        assert(validator.validate(buildToken(expOffsetMs = -(120_000L))).isLeft)
    }

    test("wrong issuer is rejected") {
        assert(validator.validate(buildToken(iss = "https://login.microsoftonline.com/other-tenant/v2.0")).isLeft)
    }

    test("wrong audience is rejected") {
        assert(validator.validate(buildToken(aud = "wrong-client-id")).isLeft)
    }

    // Token signed with a key whose kid is not in the JWKS — verifier finds no matching key.
    test("unknown signing key is rejected") {
        assert(validator.validate(buildToken(signingKey = otherKey)).isLeft)
    }

    test("malformed token string is rejected") {
        assert(validator.validate("not.a.valid.jwt").isLeft)
    }

    test("empty string is rejected") {
        assert(validator.validate("").isLeft)
    }
}
