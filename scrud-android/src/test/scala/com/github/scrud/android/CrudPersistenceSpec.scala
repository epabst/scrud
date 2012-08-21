package com.github.scrud.android

import common.PlatformTypes._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import org.scalatest.Spec
import com.github.scrud.android.common.UriPath
import persistence.{ReadOnlyPersistence, MutableIdPk}

/** A behavior specification for [[com.github.scrud.android.persistence.EntityPersistence]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class CrudPersistenceSpec extends Spec with MustMatchers {
  class MyEntity(givenId: Option[ID] = None) extends MutableIdPk {
    this.id = givenId
  }
  val persistence = new SeqCrudPersistence[MyEntity] with ReadOnlyPersistence {
    def entityType = MyEntityType
    def crudContext = null
    def findAll(uri: UriPath) = Seq(new MyEntity(entityType.UriPathId.getValue(uri)))
    def listeners = Set.empty
  }

  it("find must set IdPk.id") {
    val uri = persistence.entityType.toUri(100L)
    val Some(result) = persistence.find(uri, new MyEntity)
    result.id must be (Some(100L))
  }

  it("findAll must set IdPk.id") {
    val uri = persistence.entityType.toUri(100L)
    val result = persistence.findAll(uri, new MyEntity).head
    result.id must be (Some(100L))
  }
}