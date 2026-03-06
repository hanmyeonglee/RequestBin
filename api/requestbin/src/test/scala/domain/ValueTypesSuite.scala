package domain

import munit.FunSuite
import domain.entity.{Body, Headers, Query}
import scala.collection.immutable.ArraySeq

class ValueTypesSuite extends FunSuite {

    // Query
    test("Query.size returns sum of all key and value lengths") {
        val q = Query(Map("foo" -> List("bar", "baz"), "k" -> List("v")))
        val expected = "foo".length + "bar".length + "baz".length + "k".length + "v".length
        assertEquals(q.size, expected)
    }

    test("Query.size returns 0 for empty params") {
        assertEquals(Query(Map.empty).size, 0)
    }

    // Headers
    test("Headers.size returns sum of all key and value lengths") {
        val h = Headers(Map("Content-Type" -> "application/json", "X-Id" -> "123"))
        val expected = "Content-Type".length + "application/json".length + "X-Id".length + "123".length
        assertEquals(h.size, expected)
    }

    test("Headers.size returns 0 for empty entries") {
        assertEquals(Headers(Map.empty).size, 0)
    }

    // Body
    test("Body.size returns the byte count") {
        val b = Body(ArraySeq[Byte](1, 2, 3))
        assertEquals(b.size, 3)
    }

    test("Body.size returns 0 for empty bytes") {
        assertEquals(Body(ArraySeq.empty).size, 0)
    }

    test("Body.fromArrayBytes constructs Body correctly") {
        val arr  = Array[Byte](10, 20, 30)
        val body = Body.fromArrayBytes(arr)
        assertEquals(body.bytes.toArray.toSeq, arr.toSeq)
    }
}
