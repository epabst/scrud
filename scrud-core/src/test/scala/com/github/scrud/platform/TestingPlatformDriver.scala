package com.github.scrud.platform

import com.github.scrud.persistence.ListBufferPersistenceFactoryForTesting
import com.github.scrud.action.CrudOperationType._
import com.github.scrud.types.QualifiedType
import com.github.scrud.EntityName
import com.github.scrud.action.CommandKey
import com.github.scrud.action.Command
import com.github.scrud.util.Name
import com.netaporter.uri.Uri
import scala.util.Success

/**
 * A simple PlatformDriver for testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/28/12
 *         Time: 1:27 PM
 */
class TestingPlatformDriver extends PlatformDriver {
  protected def logTag = getClass.getSimpleName

  override def tryResource(resourceName: Name) = Success(Uri.parse("image:" + resourceName.toCamelCase))

  val localDatabasePersistenceFactory = ListBufferPersistenceFactoryForTesting

  private object PersistenceFieldFactory extends PersistenceAdaptableFieldFactory {
    def sourceField[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V]) =
      MapStorageAdaptableFieldFactory.createSourceField(entityName, fieldName, qualifiedType)

    def targetField[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V]) =
      MapStorageAdaptableFieldFactory.createTargetField(entityName, fieldName, qualifiedType)
  }

  def idFieldName(entityName: EntityName): String = "id"

  def commandToAddItem(entityName: EntityName) = Command(CommandKey("Add"), None, None)

  def commandToDeleteItem(entityName: EntityName) = Command(CommandKey("Delete"), None, None)

  def commandToDisplayItem(entityName: EntityName) = Command(CommandKey("View"), None, None)

  def commandToEditItem(entityName: EntityName) = Command(CommandKey("Edit"), None, None)

  def commandToListItems(entityName: EntityName) = Command(CommandKey("List"), None, None)

  /** An Operation that will show the UI to the user for creating an entity instance. */
  def operationToShowCreateUI(entityName: EntityName) =
    CrudOperationForTesting(entityName, Create)

  /** An Operation that will show the UI to the user that displays an entity instance. */
  def operationToShowDisplayUI(entityName: EntityName) =
    CrudOperationForTesting(entityName, Read)

  /** An Operation that will show the UI to the user that lists the entity instances. */
  def operationToShowListUI(entityName: EntityName) =
    CrudOperationForTesting(entityName, List)

  /** An Operation that will show the UI to the user for updating an entity instance. */
  def operationToShowUpdateUI(entityName: EntityName) =
    CrudOperationForTesting(entityName, Update)

  /** The command to undo the last delete. */
  def commandToUndoDelete = Command(CommandKey("command1"), None, None)

  val platformSpecificFieldFactories = Seq(MapStorageAdaptableFieldFactory, PersistenceFieldFactory)
}

object TestingPlatformDriver extends TestingPlatformDriver
