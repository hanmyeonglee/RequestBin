package domain.entity

// Immutable representation of HTTP headers as ordered key-value pairs
case class Headers(entries: Map[String, String]) {
    def size: Int = entries.foldLeft(0) { case (acc, (k, v)) => acc + k.length + v.length }
}
