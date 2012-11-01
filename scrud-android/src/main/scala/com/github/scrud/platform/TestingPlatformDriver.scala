package com.github.scrud.platform

import com.github.scrud.persistence.ListBufferPersistenceFactory
import com.github.scrud.{CrudContext, UriPath, EntityName}
import com.github.scrud.action.{Operation, CrudOperationType}

/**
 * A simple PlatformDriver for testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/28/12
 *         Time: 1:27 PM
 */
class TestingPlatformDriver extends PlatformDriver {
  protected def logTag = getClass.getSimpleName

  val localDatabasePersistenceFactory = new ListBufferPersistenceFactory[AnyRef](Map.empty[String,Any])

  /** An Operation that will show the UI to the user for creating an entity instance. */
  def operationToShowCreateUI(entityName: EntityName) =
    ShowEntityUIOperationForTesting(entityName, CrudOperationType.Create)

  /** An Operation that will show the UI to the user that displays an entity instance. */
  def operationToShowDisplayUI(entityName: EntityName) =
    ShowEntityUIOperationForTesting(entityName, CrudOperationType.Read)

  /** An Operation that will show the UI to the user that lists the entity instances. */
  def operationToShowListUI(entityName: EntityName) =
    ShowEntityUIOperationForTesting(entityName, CrudOperationType.List)

  /** An Operation that will show the UI to the user for updating an entity instance. */
  def operationToShowUpdateUI(entityName: EntityName) =
    ShowEntityUIOperationForTesting(entityName, CrudOperationType.Update)
}

object TestingPlatformDriver extends TestingPlatformDriver

case class ShowEntityUIOperationForTesting(entityName: EntityName, operationType: CrudOperationType.Value) extends Operation {
  /** Runs the operation, given the uri and the current CrudContext. */
  def invoke(uri: UriPath, crudContext: CrudContext) {
    println("Showing Entity UI Operation: entityName=" + entityName + " operation=" + operationType)
  }
}
