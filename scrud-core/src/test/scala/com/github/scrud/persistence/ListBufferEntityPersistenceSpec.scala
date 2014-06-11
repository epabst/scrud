package com.github.scrud.persistence

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import com.github.scrud.{UriPath, EntityName}
import com.github.scrud.util.MutableListenerSet
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * A behavior specification for [[com.github.scrud.persistence.ListBufferEntityPersistence]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 1/5/13
 * Time: 12:14 AM
 */
@RunWith(classOf[JUnitRunner])
class ListBufferEntityPersistenceSpec extends FunSpec with MustMatchers {
  describe("findAll") {
    it("must find the entry with the same id") {
      val persistence = new ListBufferEntityPersistence[TestEntity](TestEntity, new TestEntity(""), new MutableListenerSet())
      persistence.save(None, new TestEntity("a"))
      val id2 = persistence.save(None, new TestEntity("b"))
      persistence.save(None, new TestEntity("c"))
      persistence.findAll(TestEntity.toUri(id2)) must be (Seq(TestEntity("b")))
    }

    it("must find no entry if none with the same id") {
      val persistence = new ListBufferEntityPersistence[TestEntity](TestEntity, new TestEntity(""), new MutableListenerSet())
      persistence.save(None, new TestEntity("a"))
      persistence.save(None, new TestEntity("b"))
      persistence.findAll(TestEntity.toUri(-9000L)) must be (Nil)
    }

    it("must find all entries if no id specified in uri") {
      val persistence = new ListBufferEntityPersistence[TestEntity](TestEntity, new TestEntity(""), new MutableListenerSet())
      persistence.save(None, new TestEntity("a"))
      persistence.save(None, new TestEntity("b"))
      persistence.findAll(UriPath.EMPTY) must be (Seq(TestEntity("b"), TestEntity("a")))
    }

    it("must find all entries if no id specified in uri and entries were saved with ids") {
      val persistence = new ListBufferEntityPersistence[TestEntity](TestEntity, new TestEntity(""), new MutableListenerSet())
      persistence.save(Some(101L), new TestEntity("a"))
      persistence.save(Some(102L), new TestEntity("b"))
      persistence.findAll(UriPath.EMPTY) must be (Seq(TestEntity("b"), TestEntity("a")))
    }
  }
}

case class TestEntity(name: String)

object TestEntity extends EntityName("TestEntity")
