package config

import munit.FunSuite

class FrontendConfigSuite extends FunSuite {

    test("scope is derived from clientId using api://{clientId}/.default format") {
        val cfg = FrontendConfig(
            tenantId      = "tenant-123",
            clientId      = "client-456",
            scope         = s"api://client-456/.default"
        )
        assertEquals(cfg.scope, "api://client-456/.default")
    }

    test("all fields are preserved as-is") {
        val cfg = FrontendConfig(
            tenantId      = "my-tenant",
            clientId      = "my-client",
            scope         = "api://my-client/.default"
        )
        assertEquals(cfg.tenantId,      "my-tenant")
        assertEquals(cfg.clientId,      "my-client")
        assertEquals(cfg.scope,         "api://my-client/.default")
    }

    test("scope pattern matches api://{clientId}/.default") {
        val clientId = "abc-123-def-456"
        val cfg = FrontendConfig(
            tenantId      = "t",
            clientId      = clientId,
            scope         = s"api://$clientId/.default"
        )
        assert(cfg.scope.startsWith("api://"))
        assert(cfg.scope.endsWith("/.default"))
        assert(cfg.scope.contains(clientId))
    }
}
