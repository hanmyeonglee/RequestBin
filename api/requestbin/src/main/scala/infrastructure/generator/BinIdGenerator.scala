package infrastructure.generator

import domain.shared.Generator
import java.security.SecureRandom

class BinIdGenerator extends Generator[String] {
    private val random = new SecureRandom()
    override def generate: String =
        LazyList
            .continually(random.nextInt(26))
            .map(i => ('a' + i).toChar)
            .take(12)
            .mkString
}
