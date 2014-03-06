package com.github.scrud

import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner

/** A behavior specification for [[com.github.scrud.UriPath]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class UriPathSpec extends FunSpec with MustMatchers {
  val entityName = EntityTypeForTesting.entityName

  describe("findId") {
    it("must find the id following the entity name") {
      UriPath.findId(UriPath("foo"), entityName) must be (None)
      UriPath.findId(UriPath(entityName), entityName) must be (None)
      UriPath.findId(UriPath(entityName, 123), entityName) must be (Some(123))
      UriPath.findId(UriPath(entityName, 123) / "foo", entityName) must be (Some(123))
      UriPath.findId(UriPath(entityName) / "blah", entityName) must be (None)
    }
  }

  describe("lastEntityNameOption") {
    it("must handle an empty UriPath") {
      UriPath.lastEntityNameOption(UriPath.EMPTY)  must be (None)
    }

    it("must get the last entityName") {
      UriPath.lastEntityNameOption(UriPath("a", "b", "c"))  must be (Some(EntityName("c")))
    }

    it("must get the last entityName even if followed by an ID") {
      UriPath.lastEntityNameOption(UriPath("a", "1"))  must be (Some(EntityName("a")))
      UriPath.lastEntityNameOption(UriPath("a", "1", "b", "c", "3"))  must be (Some(EntityName("c")))
    }
  }

  describe("specifyLastEntityName") {
    it("must handle an empty UriPath") {
      UriPath.specifyLastEntityName(UriPath.EMPTY, EntityName("a")) must be (UriPath("a"))
    }

    it("must handle the case where it already ends with the EntityName") {
      UriPath.specifyLastEntityName(UriPath("a", "b"), EntityName("b")) must be (UriPath("a", "b"))
    }

    it("must handle the case where it already ends with the EntitName followed by an ID") {
      UriPath.specifyLastEntityName(UriPath("a", "b", "1"), EntityName("b")) must be (UriPath("a", "b", "1"))
    }

    it("must strip off whatever is after the ID") {
      val uri = UriPath("abc", "123", entityName.name, "456", "def")
      UriPath.specifyLastEntityName(uri, entityName) must be (UriPath("abc", "123", entityName.name, "456"))
    }

    it("must not fail if no ID found but also preserve what is there already") {
      val uri = UriPath("abc", "123", "def")
      UriPath.specifyLastEntityName(uri, entityName).segments.startsWith(uri.segments) must be (true)
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
