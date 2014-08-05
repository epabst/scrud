package com.github.scrud.platform

import com.github.scrud.persistence.{PersistenceFactory, ListBufferPersistenceFactory}
import com.github.scrud.action.CrudOperationType._
import com.github.scrud.EntityName
import com.github.scrud.action.{Operation, ActionKey, PlatformCommand}
import com.github.scrud.util.Name
import java.net.URI
import scala.util.{Try, Success}
import com.github.scrud.platform.PlatformTypes.{ImgKey, SKey}
import com.github.scrud.copy.types.MapStorage

/**
 * A simple PlatformDriver for use until a real implementation is provided.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/28/12
 *         Time: 1:27 PM
 */
class StubPlatformDriver extends PlatformDriver {
  override def tryBinaryResource(resourceName: Name): Try[URI] = Success(URI.create("image:" + resourceName.toCamelCase))

  override def tryStringKey(stringName: String): Try[SKey] = Success(stringName.hashCode)

  override def tryImageKey(imageName: Name): Try[ImgKey] = Success(imageName.hashCode())

  val localDatabasePersistenceFactory: PersistenceFactory = new ListBufferPersistenceFactory[MapStorage](new MapStorage)

  def idFieldName(entityName: EntityName): String = "id"

  def commandToAddItem(entityName: EntityName) = PlatformCommand(ActionKey.Create, None, None)

  def commandToDeleteItem(entityName: EntityName) = PlatformCommand(ActionKey.Delete, None, None)

  def commandToDisplayItem(entityName: EntityName) = PlatformCommand(ActionKey.View, None, None)

  def commandToEditItem(entityName: EntityName) = PlatformCommand(ActionKey.Update, None, None)

  def commandToListItems(entityName: EntityName) = PlatformCommand(ActionKey.View, None, None)

  /** An Operation that will show the UI to the user for creating an entity instance. */
  def operationToShowCreateUI(entityName: EntityName): Operation =
    StubOperation(entityName, Create)

  /** An Operation that will show the UI to the user that displays an entity instance. */
  def operationToShowDisplayUI(entityName: EntityName): Operation =
    StubOperation(entityName, Read)

  /** An Operation that will show the UI to the user that lists the entity instances. */
  def operationToShowListUI(entityName: EntityName): Operation =
    StubOperation(entityName, List)

  /** An Operation that will show the UI to the user for updating an entity instance. */
  def operationToShowUpdateUI(entityName: EntityName): Operation =
    StubOperation(entityName, Update)

  /** The command to undo the last delete. */
  def commandToUndoDelete = PlatformCommand(ActionKey("command1"), None, None)

  val platformSpecificFieldFactories: Seq[AdaptableFieldFactory] = Seq.empty
}

object StubPlatformDriver extends StubPlatformDriver
