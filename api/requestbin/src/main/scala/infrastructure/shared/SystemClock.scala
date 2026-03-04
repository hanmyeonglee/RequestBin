package infrastructure.shared

import domain.shared.Clock

class SystemClock extends Clock {
    override def now: Long = System.currentTimeMillis()
}
