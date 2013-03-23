package com.github.scrud

import org.junit.runner.RunWith
import android.EntityTypeForTesting
import org.scalatest.matchers.MustMatchers
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec

/** A behavior specification for [[com.github.scrud.UriPath]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class UriPathSpec extends FunSpec with MustMatchers {
  val entityName = EntityTypeForTesting.entityName

  describe("findId") {
    it("must find the id following the entity name") {
      UriPath("foo").findId(entityName) must be (None)
      UriPath(entityName).findId(entityName) must be (None)
      UriPath(entityName, 123).findId(entityName) must be (Some(123))
      (UriPath(entityName, 123) / "foo").findId(entityName) must be (Some(123))
      (UriPath(entityName) / "blah").findId(entityName) must be (None)
    }
  }

  describe("lastEntityNameOption") {
    it("must handle an empty UriPath") {
      UriPath.EMPTY.lastEntityNameOption must be (None)
    }

    it("must get the last entityName") {
      UriPath("a", "b", "c").lastEntityNameOption must be (Some(EntityName("c")))
    }

    it("must get the last entityName even if followed by an ID") {
      UriPath("a", "1").lastEntityNameOption must be (Some(EntityName("a")))
      UriPath("a", "1", "b", "c", "3").lastEntityNameOption must be (Some(EntityName("c")))
    }
  }

  describe("upToOptionalIdOf") {
    it("must strip of whatever is after the ID") {
      val uri = UriPath("abc", "123", entityName.name, "456", "def")
      uri.upToOptionalIdOf(entityName) must be (UriPath("abc", "123", entityName.name, "456"))
    }

    it("must not fail if no ID found but also preserve what is there already") {
      val uri = UriPath("abc", "123", "def")
      uri.upToOptionalIdOf(entityName).segments.startsWith(uri.segments) must be (true)
    }
  }

  describe("upToIdOf") {
    it("must strip of whatever is after the ID") {
      val uri = UriPath("abc", "123", entityName.name, "456", "def")
      uri.upToIdOf(entityName) must be (Some(UriPath("abc", "123", entityName.name, "456")))
    }

    it("must return None if no entityName not found") {
      val uri = UriPath("abc", "123", "def")
      uri.upToIdOf(entityName) must be (None)
    }

    it("must return None if no ID specified") {
      val uri = UriPath("abc", "123", entityName.name)
      uri.upToIdOf(entityName) must be (None)
    }
  }

  it("must convert from a string") {
    val uriPath = UriPath("abc", "123", "def")
    UriPath(uriPath.toString) must be (uriPath)
  }

  it("must convert from an empty uri") {
    UriPath(UriPath.EMPTY.toString) must be(UriPath.EMPTY)
  }

  it ("must convert from an empty string") {
    UriPath("") must be (UriPath.EMPTY)
  }
}
