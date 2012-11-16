package com.github.scrud

import action.{CrudOperationType, CrudOperation}
import android.{MyCrudApplication, MyCrudType, MyEntityType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import platform.TestingPlatformDriver

/** A behavior specification for [[com.github.scrud.CrudApplication]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class CrudApplicationSpec extends FunSpec with MustMatchers {

  it("must provide a valid nameId") {
    val application = new CrudApplication(TestingPlatformDriver) {
      val name = "A diFFicult name to use as an ID"
      val allCrudTypes = List()
      val dataVersion = 1
    }
    application.nameId must be ("a_difficult_name_to_use_as_an_id")
  }

  it("must get the correct entity actions with child entities") {
    val parentEntityName = new EntityName("Entity1")
    val parentEntityType = new MyEntityType(parentEntityName)
    val childEntityName = new EntityName("Child")
    val childEntityType = new MyEntityType(childEntityName) {
      override val valueFields = ParentField(parentEntityType) :: super.valueFields
    }
    val childCrudType = new MyCrudType(childEntityType)
    val parentCrudType = new MyCrudType(parentEntityType)
    val application = MyCrudApplication(childCrudType, parentCrudType)
    application.actionsFromCrudOperation(CrudOperation(childEntityName, CrudOperationType.Read)) must be (
      List(application.actionToUpdate(childEntityType).get, application.actionToDelete(childEntityType).get))
    application.actionsFromCrudOperation(CrudOperation(parentEntityName, CrudOperationType.Read)) must be (
      List(application.actionToList(childEntityType).get, application.actionToUpdate(parentEntityType).get, application.actionToDelete(parentEntityType).get))
  }

  it("must get the correct list actions with child entities") {
    val parentEntityName = EntityName("Parent")
    val parentEntityType = new MyEntityType(parentEntityName)
    val childEntityName1 = EntityName("Child1")
    val childEntityType1 = new MyEntityType(childEntityName1) {
      override lazy val valueFields = ParentField(parentEntityType) :: super.valueFields
    }
    val childEntityType2 = new MyEntityType(EntityName("Child2")) {
      override lazy val valueFields = ParentField(parentEntityType) :: super.valueFields
    }
    val parentCrudType = new MyCrudType(parentEntityType)
    val childCrudType1 = new MyCrudType(childEntityType1)
    val childCrudType2 = new MyCrudType(childEntityType2)
    val application = new MyCrudApplication(childCrudType1, childCrudType2, parentCrudType) {
      override def hasDisplayPage(entityName: EntityName): Boolean = parentEntityType.entityName == entityName
    }
    application.actionsFromCrudOperation(CrudOperation(parentEntityName, CrudOperationType.List)) must be (
      List(application.actionToCreate(parentEntityType).get))
    application.actionsFromCrudOperation(CrudOperation(childEntityName1, CrudOperationType.List)) must be (
      List(application.actionToCreate(childEntityType1).get))
  }

  it("must get the correct list actions with child entities w/ no parent display") {
    val parentEntityName = EntityName("Parent")
    val parentEntityType = new MyEntityType(parentEntityName)
    val childEntityName1 = EntityName("Child1")
    val childEntityType1 = new MyEntityType(childEntityName1) {
      override lazy val valueFields = ParentField(parentEntityType) :: super.valueFields
    }
    val childEntityType2 = new MyEntityType(EntityName("Child2")) {
      override lazy val valueFields = ParentField(parentEntityType) :: super.valueFields
    }
    val parentCrudType = new MyCrudType(parentEntityType)
    val childCrudType1 = new MyCrudType(childEntityType1)
    val childCrudType2 = new MyCrudType(childEntityType2)
    val application = MyCrudApplication(parentCrudType, childCrudType1, childCrudType2)
    application.actionsFromCrudOperation(CrudOperation(parentEntityName, CrudOperationType.List)) must be (List(application.actionToCreate(parentEntityType).get))
    application.actionsFromCrudOperation(CrudOperation(childEntityName1, CrudOperationType.List)) must be (
      List(application.actionToCreate(childEntityType1).get, application.actionToUpdate(parentEntityType).get, application.actionToList(childEntityType2).get))
  }
}
