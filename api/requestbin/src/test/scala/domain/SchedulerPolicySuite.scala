package domain

import munit.FunSuite
import domain.policy.SchedulerPolicy

class SchedulerPolicySuite extends FunSuite {
    private val policy = SchedulerPolicy(ttlSeconds = 900L, intervalSeconds = 300L, cleanUpTimeHour = 3)

    // 3:00 AM in Unix seconds from epoch: any T where T % 86400 / 3600 == 3
    private val atCleanHour  = 86400L + 10800L   // day 2, 3:00 AM
    private val notCleanHour = 86400L + 14400L   // day 2, 4:00 AM

    test("isCertainTimeHour returns true at the configured hour") {
        assert(policy.isCertainTimeHour(atCleanHour))
    }

    test("isCertainTimeHour returns false at a different hour") {
        assert(!policy.isCertainTimeHour(notCleanHour))
    }

    test("isFirstCleanTime returns true when more than 12 hours have passed") {
        // 50000 - 0 = 50000 > 43200
        assert(policy.isFirstCleanTime(50000L, 0L))
    }

    test("isFirstCleanTime returns false when fewer than 12 hours have passed") {
        // 43199 - 0 = 43199, not > 43200
        assert(!policy.isFirstCleanTime(43199L, 0L))
    }

    test("isFirstCleanTime returns false at exactly 12 hours (strict greater-than)") {
        // 43200 - 0 = 43200, not > 43200
        assert(!policy.isFirstCleanTime(43200L, 0L))
    }
}
