package com.github.scrud.android

import com.github.scrud.{ParentField, EntityName, UriPath}
import org.junit.runner.RunWith
import persistence.CursorField
import scala.collection.mutable
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import org.mockito._
import Mockito._
import Matchers._
import com.github.triangle.PortableField._
import ForeignKey.foreignKey
import com.github.scrud.state.State
import com.github.scrud.persistence.CrudPersistence
import com.github.scrud.util.CrudMockitoSugar

/** A behavior specification for [[com.github.scrud.android.CrudType]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class CrudTypeSpec extends FunSpec with MustMatchers with CrudMockitoSugar {

  it("must force having an id field on subtypes") {
    val crudType = new MyEntityType {
      override def valueFields = List(mapField[String]("name"))
    }
    crudType.deepCollect {
      case f if f == crudType.UriPathId => Some(true)
    }.flatten must be (Seq(true))
  }

  it("must derive parent entities from ParentField fields") {
    val entityType1 = new MyEntityType(new EntityName("Entity1"))
    val entityType2 = new MyEntityType(new EntityName("Entity2"))
    val crudType3 = new MyCrudType(new MyEntityType {
      override val valueFields = ParentField(entityType1) +: ParentField(entityType2) +: super.valueFields
    })
    val application = MyCrudApplication(new MyCrudType(entityType1), new MyCrudType(entityType2), crudType3)
    crudType3.parentEntityTypes(application) must be (List(entityType1, entityType2))
  }

  it("must derive parent entities from foreignKey fields") {
    val entityType1 = new MyEntityType(new EntityName("Entity1"))
    val entityType2 = new MyEntityType(new EntityName("Entity2"))
    val crudType3 = new MyCrudType(new MyEntityType {
      override val valueFields = foreignKey(entityType1) +: foreignKey(entityType2) +: super.valueFields
    })
    val application = MyCrudApplication(new MyCrudType(entityType1), new MyCrudType(entityType2), crudType3)
    crudType3.parentEntityTypes(application) must be (List(entityType1, entityType2))
  }

  it("must get the correct entity actions with child entities") {
    val parentEntityType = new MyEntityType(new EntityName("Entity1"))
    val childEntityType = new MyEntityType {
      override lazy val valueFields = ParentField(parentEntityType) :: super.valueFields
    }
    val childCrudType = new MyCrudType(childEntityType)
    val parentCrudType = new MyCrudType(parentEntityType)
    val application = MyCrudApplication(childCrudType, parentCrudType)
    application.actionsForEntity(childEntityType) must be (List(application.actionToUpdate(childEntityType).get, application.actionToDelete(childEntityType).get))
    application.actionsForEntity(parentEntityType) must be (
      List(application.actionToList(childEntityType).get, application.actionToUpdate(parentEntityType).get, application.actionToDelete(parentEntityType).get))
  }

  it("must get the correct list actions with child entities") {
    val parentEntityType = new MyEntityType(EntityName("Parent"))
    val childEntityType1 = new MyEntityType(EntityName("Child1")) {
      override lazy val valueFields = ParentField(parentEntityType) :: super.valueFields
    }
    val childEntityType2 = new MyEntityType(EntityName("Child2")) {
      override lazy val valueFields = ParentField(parentEntityType) :: super.valueFields
    }
    val parentCrudType = new MyCrudType(parentEntityType)
    val childCrudType1 = new MyCrudType(childEntityType1)
    val childCrudType2 = new MyCrudType(childEntityType2)
    val application = new MyCrudApplication(childCrudType1, childCrudType2, parentCrudType) {
      override def hasDisplayPage(entityName: EntityName): Boolean = parentCrudType.entityName == entityName
    }
    application.actionsForList(parentEntityType) must be (List(application.actionToCreate(parentEntityType).get))
    application.actionsForList(childEntityType1) must be (List(application.actionToCreate(childEntityType1).get))
  }

  it("must get the correct list actions with child entities w/ no parent display") {
    val parentEntityType = new MyEntityType(EntityName("Parent"))
    val childEntityType1 = new MyEntityType(EntityName("Child1")) {
      override lazy val valueFields = ParentField(parentEntityType) :: super.valueFields
    }
    val childEntityType2 = new MyEntityType(EntityName("Child2")) {
      override lazy val valueFields = ParentField(parentEntityType) :: super.valueFields
    }
    val parentCrudType = new MyCrudType(parentEntityType)
    val childCrudType1 = new MyCrudType(childEntityType1)
    val childCrudType2 = new MyCrudType(childEntityType2)
    val application = MyCrudApplication(parentCrudType, childCrudType1, childCrudType2)
    application.actionsForList(parentEntityType) must be (List(application.actionToCreate(parentEntityType).get))
    application.actionsForList(childEntityType1) must be (
      List(application.actionToUpdate(parentEntityType).get, application.actionToList(childEntityType2).get, application.actionToCreate(childEntityType1).get))
  }
}
