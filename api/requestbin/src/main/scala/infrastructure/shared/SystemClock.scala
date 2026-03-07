package infrastructure.shared

import domain.shared.Clock
import java.time.Instant

class SystemClock extends Clock {
    override def now(): Instant = Instant.now()
}
