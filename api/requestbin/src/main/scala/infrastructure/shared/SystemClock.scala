package infrastructure.shared

import domain.shared.Clock

class SystemClock extends Clock {
    override def currentUnixTimeSeconds: Long = System.currentTimeMillis() / 1000 + 32400 // UTC+9
}
