package domain

import munit.FunSuite
import domain.entity.{Bin, Body, CapturedRequest, Headers, Query}
import scala.collection.immutable.ArraySeq

class BinSuite extends FunSuite {
    private val now = 10000L
    private val ttl = 200L

    private val testRequest = CapturedRequest(
        method     = "GET",
        path       = "/",
        query      = Query(Map.empty),
        headers    = Headers(Map.empty),
        body       = Body(ArraySeq.empty),
        remoteHost = "127.0.0.1",
        createdAt  = 0L
    )

    test("isExpired returns false when within TTL") {
        val bin = Bin("abc", now - 100L)
        assert(!bin.isExpired(now, ttl))
    }

    test("isExpired returns true when beyond TTL") {
        val bin = Bin("abc", now - 300L)
        assert(bin.isExpired(now, ttl))
    }

    test("isExpired returns false at exactly the TTL boundary") {
        // currentTime - lastUsed == ttl: 200 - 200 is NOT > ttl, so not expired
        val bin = Bin("abc", now - ttl)
        assert(!bin.isExpired(now, ttl))
    }

    test("canAcceptRequest returns true when bin is not expired") {
        val bin = Bin("abc", now - 100L)
        assert(bin.canAcceptRequest(testRequest, now, ttl))
    }

    test("canAcceptRequest returns false when bin is expired") {
        val bin = Bin("abc", now - 300L)
        assert(!bin.canAcceptRequest(testRequest, now, ttl))
    }

    test("markLastUsedUnixTimeSeconds returns updated bin preserving binId") {
        val bin = Bin("abc", 500L)
        val updated = bin.markLastUsedUnixTimeSeconds(now)
        assertEquals(updated.binId, "abc")
        assertEquals(updated.lastUsedAtUnixTimeSeconds, now)
    }
}
