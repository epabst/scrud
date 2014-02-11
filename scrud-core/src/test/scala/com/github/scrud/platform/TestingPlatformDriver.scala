package com.github.scrud.platform

import com.github.scrud.persistence.ListBufferPersistenceFactoryForTesting
import com.github.scrud.EntityType
import com.github.scrud.action.CrudOperationType
import com.github.scrud.types.QualifiedType
import com.github.scrud.copy._
import com.github.scrud.EntityName
import com.github.scrud.action.CommandId
import com.github.scrud.action.Command
import com.github.scrud.platform.representation.Representation
import com.github.scrud.util.Logging

/**
 * A simple PlatformDriver for testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/28/12
 *         Time: 1:27 PM
 */
class TestingPlatformDriver extends PlatformDriver with Logging {
  protected def logTag = getClass.getSimpleName

  val localDatabasePersistenceFactory = ListBufferPersistenceFactoryForTesting

  def calculateDataVersion(entityTypes: Seq[EntityType]) = 1

  def idFieldName(entityName: EntityName, primaryKey: Boolean = true): String = {
    if (primaryKey) {
      "id"
    } else {
      entityName.toCamelCase + "Id"
    }
  }

  def commandToAddItem(entityName: EntityName) = Command(CommandId("Add"), None, None)

  def commandToDeleteItem(entityName: EntityName) = Command(CommandId("Delete"), None, None)

  def commandToDisplayItem(entityName: EntityName) = Command(CommandId("View"), None, None)

  def commandToEditItem(entityName: EntityName) = Command(CommandId("Edit"), None, None)

  def commandToListItems(entityName: EntityName) = Command(CommandId("List"), None, None)

  /** An Operation that will show the UI to the user for creating an entity instance. */
  def operationToShowCreateUI(entityName: EntityName) =
    CrudOperationForTesting(entityName, CrudOperationType.Create)

  /** An Operation that will show the UI to the user that displays an entity instance. */
  def operationToShowDisplayUI(entityName: EntityName) =
    CrudOperationForTesting(entityName, CrudOperationType.Read)

  /** An Operation that will show the UI to the user that lists the entity instances. */
  def operationToShowListUI(entityName: EntityName) =
    CrudOperationForTesting(entityName, CrudOperationType.List)

  /** An Operation that will show the UI to the user for updating an entity instance. */
  def operationToShowUpdateUI(entityName: EntityName) =
    CrudOperationForTesting(entityName, CrudOperationType.Update)

  /** The command to undo the last delete. */
  def commandToUndoDelete = Command(CommandId("command1"), None, None)

  override def field[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V], representations: Seq[Representation]): ExtensibleAdaptableField[V] = {
    val resultFieldAndUnused = MapStorageAdaptableFieldFactory.adapt(entityName, fieldName, qualifiedType,
      AdaptableFieldAndUnusedRepresentations(AdaptableField.empty, representations))
    (resultFieldAndUnused.field, resultFieldAndUnused.unusedRepresentations)
    if (!resultFieldAndUnused.unusedRepresentations.isEmpty) {
      info("Representations that were not used: " + resultFieldAndUnused.unusedRepresentations.mkString(", "))
    }
    resultFieldAndUnused.field
  }
}

object TestingPlatformDriver extends TestingPlatformDriver
