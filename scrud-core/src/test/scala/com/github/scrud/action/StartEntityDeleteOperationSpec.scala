package com.github.scrud.action

import org.scalatest.FunSpec
import com.github.scrud.persistence._
import com.github.scrud.{EntityNavigationForTesting, EntityTypeForTesting, UriPath}
import com.github.scrud.util.CrudMockitoSugar
import com.github.scrud.platform.TestingPlatformDriver
import org.scalatest.matchers.MustMatchers
import com.github.scrud.context.CommandContextForTesting
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.copy.types.MapStorage

/**
 * A behavior specification for [[com.github.scrud.action.StartEntityDeleteOperation]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 11/30/12
 * Time: 3:32 PM
 */
@RunWith(classOf[JUnitRunner])
class StartEntityDeleteOperationSpec extends FunSpec with CrudMockitoSugar with MustMatchers {
  val platformDriver = TestingPlatformDriver

  it("must delete with option to undo") {
    val entityType = new EntityTypeForTesting
    val entityName = entityType.entityName
    val id = 345L
    val data = new MapStorage(entityName, entityType.idFieldName -> Some(id), "name" -> Some("George"))
    val uri = UriPath(entityName) / id
    var allowUndoCalled = false
    val entityTypeMap = EntityTypeMapForTesting(entityType)
    val commandContext = new CommandContextForTesting(entityTypeMap) {
      override def allowUndo(undoable: Undoable) {
        allowUndoCalled = true
        undoable.closeOperation.foreach(_.invoke(uri, this))
      }
    }
    commandContext.save(entityName, Some(id), MapStorage, data)

    val actionToDelete = new EntityNavigationForTesting(entityTypeMap).actionsToDelete(entityName).head
    actionToDelete.invoke(uri, commandContext)
    allowUndoCalled must be (true)
    commandContext.find(uri, MapStorage) must be (None)
  }

  it("undo must work") {
    val entityType = new EntityTypeForTesting
    val entityName = entityType.entityName
    val id = 345L
    val data = new MapStorage(entityName, entityType.idFieldName -> Some(id), "name" -> Some("George"))
    val uri = UriPath(entityName) / id
    val entityTypeMap = EntityTypeMapForTesting(entityType)
    var allowedUndoable: Option[Undoable] = None
    val commandContext = new CommandContextForTesting(entityTypeMap) {
      override def allowUndo(undoable: Undoable) {
        allowedUndoable = Some(undoable)
      }
    }
    commandContext.save(entityName, Some(id), MapStorage, data)
    val operation = new StartEntityDeleteOperation(entityType)
    operation.invoke(uri, commandContext)
    commandContext.find(uri, MapStorage) must be (None)

    allowedUndoable.get.undoAction.invoke(uri, commandContext)
    commandContext.find(uri, MapStorage) must be (
      Some(new MapStorage(entityName, entityType.idFieldName -> Some(345L), "name" -> Some("George"))))
  }
}
