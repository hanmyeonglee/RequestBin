package infrastructure.generator

import domain.shared.Generator

class BinIdGenerator extends Generator[String] {
    override def generate: String =
        LazyList
            .continually(scala.util.Random.nextInt(26))
            .map(i => ('a' + i).toChar)
            .take(10)
            .mkString
}
