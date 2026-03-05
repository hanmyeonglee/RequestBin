package domain.shared

trait Clock {
    def currentUnixTimeSeconds: Long
}
