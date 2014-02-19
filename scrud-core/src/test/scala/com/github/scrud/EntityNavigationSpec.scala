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
  val upstreamName1 = EntityName("Foo")
  val upstreamEntity1 = new EntityTypeForTesting(upstreamName1)
  val upstreamName2 = EntityName("Bar")
  val upstreamEntity2 = new EntityTypeForTesting(upstreamName2)
  val entityType = new EntityTypeForTesting() {
    field(upstreamName1, Seq(Persistence(1), EditUI))
    field(upstreamName2, Seq(Persistence(1), EditUI))
  }
  val entityName = entityType.entityName
  val entityTypeMap = EntityTypeMapForTesting(Set[EntityType](entityType, upstreamEntity1, upstreamEntity2))
  val navigation = new EntityNavigationForTesting(entityTypeMap)

  describe("actionsFromCrudOperation") {
    describe(Create.toString) {
      it("must allow Managing upstreams and Delete self") {
        navigation.actionsFromCrudOperation(CrudOperation(entityName, Create)) must be (
          navigation.actionsToManage(upstreamName1) ++ navigation.actionsToManage(upstreamName2) ++ navigation.actionsToDelete(entityName))
      }
    }

    describe(Update.toString) {
      it("must allow Display, Managing upstreams, and Delete self") {
        navigation.actionsFromCrudOperation(CrudOperation(entityName, Update)) must be (
          navigation.actionsToDisplay(entityName) ++ navigation.actionsToManage(upstreamName1) ++ navigation.actionsToManage(upstreamName2) ++ navigation.actionsToDelete(entityName))
      }
    }

    describe(Read.toString) {
      it("must allow Update and Delete self") {
        navigation.actionsFromCrudOperation(CrudOperation(entityName, Read)) must be (
          navigation.actionsToUpdate(entityName) ++ navigation.actionsToDelete(entityName))
      }

      it("must allow Listing downstreams if downstreams exist") {
        navigation.actionsFromCrudOperation(CrudOperation(upstreamName1, Read)) must be (
          navigation.actionsToList(entityName) ++ navigation.actionsToUpdate(upstreamName1) ++ navigation.actionsToDelete(upstreamName1))
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
