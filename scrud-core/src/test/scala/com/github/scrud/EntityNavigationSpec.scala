package com.github.scrud

import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.action.CrudOperationType._
import com.github.scrud.platform.representation.{EditUI, Persistence}
import com.github.scrud.persistence.EntityTypeMapForTesting
import com.github.scrud.action.CrudOperation

/**
 * A behavior specification for [[com.github.scrud.EntityNavigation]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/18/14
 *         Time: 11:01 PM
 */
@RunWith(classOf[JUnitRunner])
class EntityNavigationSpec extends FunSpec with MustMatchers {
  val parentName1 = EntityName("Foo")
  val parentEntity1 = new EntityTypeForTesting(parentName1)
  val parentName2 = EntityName("Bar")
  val parentEntity2 = new EntityTypeForTesting(parentName2)
  val entityType = new EntityTypeForTesting() {
    field(parentName1, Seq(Persistence(1), EditUI))
    field(parentName2, Seq(Persistence(1), EditUI))
  }
  val entityName = entityType.entityName
  val entityTypeMap = EntityTypeMapForTesting(Set[EntityType](entityType, parentEntity1, parentEntity2))
  val navigation = new EntityNavigationForTesting(entityTypeMap)

  describe("actionsFromCrudOperation") {
    describe(Create.toString) {
      it("must allow Managing parents and Delete self") {
        navigation.actionsFromCrudOperation(CrudOperation(entityName, Create)) must be (
          navigation.actionsToManage(parentName1) ++ navigation.actionsToManage(parentName2) ++ navigation.actionsToDelete(entityName))
      }
    }

    describe(Update.toString) {
      it("must allow Display, Managing parents, and Delete self") {
        navigation.actionsFromCrudOperation(CrudOperation(entityName, Update)) must be (
          navigation.actionsToDisplay(entityName) ++ navigation.actionsToManage(parentName1) ++ navigation.actionsToManage(parentName2) ++ navigation.actionsToDelete(entityName))
      }
    }

    describe(Read.toString) {
      it("must allow Update and Delete self") {
        navigation.actionsFromCrudOperation(CrudOperation(entityName, Read)) must be (
          navigation.actionsToUpdate(entityName) ++ navigation.actionsToDelete(entityName))
      }

      it("must allow Listing children, Update, and Delete self if children exist") {
        navigation.actionsFromCrudOperation(CrudOperation(parentName1, Read)) must be (
          navigation.actionsToList(entityName) ++ navigation.actionsToUpdate(parentName1) ++ navigation.actionsToDelete(parentName1))
      }
    }

    describe(List.toString) {
      it("must allow Create") {
        navigation.actionsFromCrudOperation(CrudOperation(entityName, List)) must be (
          navigation.actionsToCreate(entityName))
      }
    }
  }
}
