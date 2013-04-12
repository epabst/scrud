package com.github.scrud

import action.{CrudOperationType, CrudOperation}
import android.persistence.CursorField
import android.{PersistenceFactoryForTesting, CrudApplicationForTesting, CrudTypeForTesting, EntityTypeForTesting}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import persistence.CrudPersistence
import platform.TestingPlatformDriver
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._

/** A behavior specification for [[com.github.scrud.CrudApplication]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class CrudApplicationSpec extends FunSpec with MustMatchers with MockitoSugar {

  it("must provide a valid nameId") {
    val application = new CrudApplication(TestingPlatformDriver) {
      val name = "A diFFicult name to use as an ID"
      val allCrudTypes = List()
    }
    application.nameId must be ("a_difficult_name_to_use_as_an_id")
  }

  it("must get the correct read actions with child entities") {
    val parentEntityName = new EntityName("Entity1")
    val parentEntityType = new EntityTypeForTesting(parentEntityName)
    val childEntityName = new EntityName("Child")
    val childEntityType = new EntityTypeForTesting(childEntityName) {
      override val valueFields = EntityField[EntityTypeForTesting](parentEntityName) :: super.valueFields
    }
    val childCrudType = new CrudTypeForTesting(childEntityType)
    val parentCrudType = new CrudTypeForTesting(parentEntityType)
    val application = new CrudApplicationForTesting(childCrudType, parentCrudType)
    application.actionsFromCrudOperation(CrudOperation(childEntityName, CrudOperationType.Read)) must be (
      List(application.actionToUpdate(childEntityType).get, application.actionToDelete(childEntityType).get))
    application.actionsFromCrudOperation(CrudOperation(parentEntityName, CrudOperationType.Read)) must be (
      List(application.actionToList(childEntityType).get, application.actionToUpdate(parentEntityType).get, application.actionToDelete(parentEntityType).get))
  }

  it("must get the correct update actions with child entities") {
    val parentEntityName = new EntityName("Entity1")
    val parentEntityType = new EntityTypeForTesting(parentEntityName)
    val childEntityName = new EntityName("Child")
    val childEntityType = new EntityTypeForTesting(childEntityName) {
      override val valueFields = EntityField[EntityTypeForTesting](parentEntityName) :: super.valueFields
    }
    val childCrudType = new CrudTypeForTesting(childEntityType)
    val parentCrudType = new CrudTypeForTesting(parentEntityType)
    val application = new CrudApplicationForTesting(childCrudType, parentCrudType)
    application.actionsFromCrudOperation(CrudOperation(childEntityName, CrudOperationType.Update)) must be (
      List(application.actionToCreate(parentEntityName).get, application.actionToList(parentEntityName).get, application.actionToDelete(childEntityType).get))
    application.actionsFromCrudOperation(CrudOperation(parentEntityName, CrudOperationType.Update)) must be (
      List(application.actionToDelete(parentEntityType).get))
  }

  it("must get the correct list actions with child entities") {
    val parentEntityName = EntityName("Parent")
    val parentEntityType = new EntityTypeForTesting(parentEntityName)
    val childEntityName1 = EntityName("Child1")
    val childEntityType1 = new EntityTypeForTesting(childEntityName1) {
      override lazy val valueFields = EntityField[EntityTypeForTesting](parentEntityName) :: super.valueFields
    }
    val childEntityType2 = new EntityTypeForTesting(EntityName("Child2")) {
      override lazy val valueFields = EntityField[EntityTypeForTesting](parentEntityName) :: super.valueFields
    }
    val parentCrudType = new CrudTypeForTesting(parentEntityType)
    val childCrudType1 = new CrudTypeForTesting(childEntityType1)
    val childCrudType2 = new CrudTypeForTesting(childEntityType2)
    val application = new CrudApplicationForTesting(childCrudType1, childCrudType2, parentCrudType) {
      override def hasDisplayPage(entityName: EntityName): Boolean = parentEntityName == entityName
    }
    application.actionsFromCrudOperation(CrudOperation(parentEntityName, CrudOperationType.List)) must be (
      List(application.actionToCreate(parentEntityName).get))
    application.actionsFromCrudOperation(CrudOperation(childEntityName1, CrudOperationType.List)) must be (
      List(application.actionToCreate(childEntityName1).get))
  }

  it("must get the correct list actions with child entities w/ no parent display") {
    val parentEntityName = EntityName("Parent")
    val parentEntityType = new EntityTypeForTesting(parentEntityName)
    val childEntityName1 = EntityName("Child1")
    val childEntityType1 = new EntityTypeForTesting(childEntityName1) {
      override lazy val valueFields = EntityField[EntityTypeForTesting](parentEntityName) :: super.valueFields
    }
    val childEntityType2 = new EntityTypeForTesting(EntityName("Child2")) {
      override lazy val valueFields = EntityField[EntityTypeForTesting](parentEntityName) :: super.valueFields
    }
    val parentCrudType = new CrudTypeForTesting(parentEntityType)
    val childCrudType1 = new CrudTypeForTesting(childEntityType1)
    val childCrudType2 = new CrudTypeForTesting(childEntityType2)
    val application = new CrudApplicationForTesting(parentCrudType, childCrudType1, childCrudType2)
    application.actionsFromCrudOperation(CrudOperation(parentEntityName, CrudOperationType.List)) must be (List(application.actionToCreate(parentEntityType).get))
    application.actionsFromCrudOperation(CrudOperation(childEntityName1, CrudOperationType.List)) must be (
      List(application.actionToCreate(childEntityType1).get, application.actionToUpdate(parentEntityType).get, application.actionToList(childEntityType2).get))
  }

  describe("saveIfValid") {
    val entityType = new EntityTypeForTesting

    it("should support adding without finding") {
      val persistence = mock[CrudPersistence]
      val application = new CrudApplicationForTesting(entityType -> new PersistenceFactoryForTesting(persistence))
      val entity = Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25))
      val uri = UriPath(entityType.entityName)
      application.saveIfValid(entity, entityType, new CrudContextItems(uri, new SimpleCrudContext(application)))
      verify(persistence).save(None, Map[String,Option[Any]](CursorField.idFieldName -> None, "name" -> Some("Bob"), "age" -> Some(25), "uri" -> Some(uri.toString)))
      verify(persistence, never()).find(uri)
    }

    it("should support updating") {
      val persistence = mock[CrudPersistence]
      val application = new CrudApplicationForTesting(entityType -> new PersistenceFactoryForTesting(persistence))
      val entity = Map[String,Option[Any]]("name" -> Some("Bob"), "age" -> Some(25))
      val uri = UriPath(entityType.entityName) / 200
      application.saveIfValid(entity, entityType, new CrudContextItems(uri, new SimpleCrudContext(application)))
      verify(persistence).save(Some(200), Map[String,Option[Any]](CursorField.idFieldName -> Some(200), "name" -> Some("Bob"), "age" -> Some(25), "uri" -> Some(uri.toString)))
    }
  }
}
