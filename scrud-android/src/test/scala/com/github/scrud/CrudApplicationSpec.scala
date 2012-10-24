package com.github.scrud

import android.{MyCrudApplication, MyCrudType, MyEntityType}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec

/** A behavior specification for [[com.github.scrud.CrudApplication]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class CrudApplicationSpec extends FunSpec with MustMatchers {

  it("must provide a valid nameId") {
    val application = new CrudApplication {
      def name = "A diFFicult name to use as an ID"
      def allCrudTypes = List()
      def dataVersion = 1
    }
    application.nameId must be ("a_difficult_name_to_use_as_an_id")
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
      override def hasDisplayPage(entityName: EntityName): Boolean = parentEntityType.entityName == entityName
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
