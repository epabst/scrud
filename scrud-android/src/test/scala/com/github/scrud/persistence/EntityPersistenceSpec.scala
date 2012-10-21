package com.github.scrud.persistence

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.UriPath

/** A behavior specification for [[com.github.scrud.persistence.EntityPersistence]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class EntityPersistenceSpec extends FunSpec with MustMatchers {
  describe("find") {
    it("must delegate to findAll and return the first result") {
      val persistence = new SeqEntityPersistence[String] with ReadOnlyPersistence {
        def findAll(uri: UriPath) = Seq("the result")
        def toUri(id: ID) = throw new UnsupportedOperationException
        def listeners = Set.empty
      }
      val uri = UriPath()
      persistence.find(uri) must be (Some("the result"))
    }

    it("must handle no results") {
      val persistence = new SeqEntityPersistence[String] with ReadOnlyPersistence {
        def findAll(uri: UriPath) = Nil
        def toUri(id: ID) = throw new UnsupportedOperationException
        def listeners = Set.empty
      }
      val uri = UriPath()
      persistence.find(uri) must be (None)
    }

    it("must fail if multiple matches are found") {
      val persistence = new SeqEntityPersistence[String] with ReadOnlyPersistence {
        def findAll(uri: UriPath) = Seq("one", "two")
        def toUri(id: ID) = throw new UnsupportedOperationException
        def listeners = Set.empty
      }
      val uri = UriPath()
      intercept[IllegalStateException] {
        persistence.find(uri)
      }
    }
  }
}