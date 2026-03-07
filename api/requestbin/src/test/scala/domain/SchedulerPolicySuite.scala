package domain

import munit.FunSuite
import java.time.{Duration, Instant}
import domain.policy.SchedulerPolicy

class SchedulerPolicySuite extends FunSuite {
    private val policy = SchedulerPolicy(ttl = Duration.ofSeconds(900L), interval = Duration.ofSeconds(300L), cleanUpTimeHour = 3)

    // 3:00 AM KST (Asia/Seoul) = 18:00 UTC
    private val atCleanHour  = Instant.ofEpochSecond(64800L)  // 1970-01-01T18:00:00Z = 03:00 KST
    private val notCleanHour = Instant.ofEpochSecond(68400L)  // 1970-01-01T19:00:00Z = 04:00 KST

    test("isCertainTimeHour returns true at the configured hour") {
        assert(policy.isCertainTimeHour(atCleanHour))
    }

    test("isCertainTimeHour returns false at a different hour") {
        assert(!policy.isCertainTimeHour(notCleanHour))
    }

    test("isFirstCleanTime returns true when more than 12 hours have passed") {
        // 50000s > 43200s (12 hours)
        assert(policy.isFirstCleanTime(Instant.ofEpochSecond(50000L), Instant.EPOCH))
    }

    test("isFirstCleanTime returns false when fewer than 12 hours have passed") {
        // 43199s, not > 43200s
        assert(!policy.isFirstCleanTime(Instant.ofEpochSecond(43199L), Instant.EPOCH))
    }

    test("isFirstCleanTime returns false at exactly 12 hours (strict greater-than)") {
        // exactly 43200s, not > 43200s
        assert(!policy.isFirstCleanTime(Instant.ofEpochSecond(43200L), Instant.EPOCH))
    }
}
