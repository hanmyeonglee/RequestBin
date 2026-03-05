package domain.entity

import scala.collection.immutable.ArraySeq

// Immutable raw bytes of an HTTP request body
case class Body(bytes: ArraySeq[Byte]) {
    def size: Int = bytes.length
}

object Body {
    def fromArrayBytes(bytes: Array[Byte]): Body = Body(ArraySeq.from(bytes))
}
