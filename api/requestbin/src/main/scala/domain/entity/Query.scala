package domain.entity

// Immutable representation of HTTP query parameters
case class Query(params: Map[String, List[String]]) {
    def size: Int = params.foldLeft(0) { case (acc, (k, v)) => acc + k.length + v.map(_.length).sum }
}
