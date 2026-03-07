package domain

import munit.FunSuite
import java.time.{Duration, Instant}
import domain.entity.{Bin, Body, CapturedRequest, Headers, Query}
import scala.collection.immutable.ArraySeq

class BinSuite extends FunSuite {
    private val now = Instant.ofEpochSecond(10000L)
    private val ttl = Duration.ofSeconds(200L)

    private val testRequest = CapturedRequest(
        method     = "GET",
        path       = "/",
        query      = Query(Map.empty),
        headers    = Headers(Map.empty),
        body       = Body(ArraySeq.empty),
        remoteHost = "127.0.0.1",
        createdAt  = Instant.EPOCH
    )

    test("isExpired returns false when within TTL") {
        val bin = Bin("abc", now.minus(Duration.ofSeconds(100L)))
        assert(!bin.isExpired(now, ttl))
    }

    test("isExpired returns true when beyond TTL") {
        val bin = Bin("abc", now.minus(Duration.ofSeconds(300L)))
        assert(bin.isExpired(now, ttl))
    }

    test("isExpired returns false at exactly the TTL boundary") {
        // Duration.between(lastUsed, now) == ttl: NOT > ttl, so not expired
        val bin = Bin("abc", now.minus(ttl))
        assert(!bin.isExpired(now, ttl))
    }

    test("canAcceptRequest returns true when bin is not expired") {
        val bin = Bin("abc", now.minus(Duration.ofSeconds(100L)))
        assert(bin.canAcceptRequest(testRequest, now, ttl))
    }

    test("canAcceptRequest returns false when bin is expired") {
        val bin = Bin("abc", now.minus(Duration.ofSeconds(300L)))
        assert(!bin.canAcceptRequest(testRequest, now, ttl))
    }

    test("markLastUsed returns updated bin preserving binId") {
        val bin = Bin("abc", Instant.ofEpochSecond(500L))
        val updated = bin.markLastUsed(now)
        assertEquals(updated.binId, "abc")
        assertEquals(updated.lastUsedAt, now)
    }
}
