package com.github.scrud.persistence

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import com.github.scrud.{UriPath, EntityName, IdPk}
import com.github.scrud.platform.PlatformTypes
import com.github.scrud.util.MutableListenerSet
//import org.junit.runner.RunWith

/**
 * A behavior specification for [[com.github.scrud.persistence.ListBufferEntityPersistence]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 1/5/13
 * Time: 12:14 AM
 */
//@RunWith(classOf[JUnitRunner])
class ListBufferEntityPersistenceSpec extends FunSpec with MustMatchers {
  describe("findAll") {
    it("must find the entry with the same id") {
      val persistence = new ListBufferEntityPersistence[TestEntity](TestEntity, new TestEntity(None), new MutableListenerSet())
      persistence.save(None, new TestEntity(None))
      val id2 = persistence.save(None, new TestEntity(None))
      persistence.save(None, new TestEntity(None))
      persistence.findAll(TestEntity.toUri(id2)) must be (Seq(TestEntity(Some(id2))))
    }

    it("must find no entry if none with the same id") {
      val persistence = new ListBufferEntityPersistence[TestEntity](TestEntity, new TestEntity(None), new MutableListenerSet())
      persistence.save(None, new TestEntity(None))
      persistence.save(None, new TestEntity(None))
      persistence.findAll(TestEntity.toUri(-9000L)) must be (Nil)
    }

    it("must find all entries if no id specified in uri") {
      val persistence = new ListBufferEntityPersistence[TestEntity](TestEntity, new TestEntity(None), new MutableListenerSet())
      val id1 = persistence.save(None, new TestEntity(None))
      val id2 = persistence.save(None, new TestEntity(None))
      persistence.findAll(UriPath.EMPTY) must be (Seq(TestEntity(Some(id2)), TestEntity(Some(id1))))
    }

    it("must find all entries if no id specified in uri and entries were saved with ids") {
      val persistence = new ListBufferEntityPersistence[TestEntity](TestEntity, new TestEntity(None), new MutableListenerSet())
      persistence.save(Some(101L), new TestEntity(Some(101L)))
      persistence.save(Some(102L), new TestEntity(Some(102L)))
      persistence.findAll(UriPath.EMPTY) must be (Seq(TestEntity(Some(102L)), TestEntity(Some(101L))))
    }
  }
}

case class TestEntity(id: Option[PlatformTypes.ID]) extends IdPk {
  def withId(id: Option[PlatformTypes.ID]) = copy(id = id)
}

object TestEntity extends EntityName("TestEntity")
