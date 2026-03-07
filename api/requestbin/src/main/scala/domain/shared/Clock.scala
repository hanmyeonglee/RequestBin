package domain.shared

import java.time.Instant

trait Clock {
    def now(): Instant
}
