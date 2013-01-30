package com.github.scrud.android

import com.github.scrud
import scrud.action.{CommandId, Command}
import scrud.android.action.{StartEntityIdActivityOperation, StartEntityActivityOperation}
import action.AndroidOperation._
import com.github.scrud.platform.PlatformDriver
import scrud.EntityName
import view.ViewField._
import com.github.triangle.PortableField
import view.ViewRef

/**
 * A PlatformDriver for the Android platform.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/28/12
 *         Time: 10:23 AM
 */
class AndroidPlatformDriver(rClass: Class[_]) extends PlatformDriver {
  lazy val localDatabasePersistenceFactory = new SQLitePersistenceFactory

  val activityClass = classOf[CrudActivity]

  /** An Operation that will show the UI to the user for creating an entity instance. */
  def operationToShowCreateUI(entityName: EntityName) =
    new StartEntityActivityOperation(entityName, CreateActionName, activityClass)

  /** An Operation that will show the UI to the user that lists the entity instances. */
  def operationToShowListUI(entityName: EntityName) =
    new StartEntityActivityOperation(entityName, ListActionName, activityClass)

  /** An Operation that will show the UI to the user that displays an entity instance. */
  def operationToShowDisplayUI(entityName: EntityName) =
    new StartEntityIdActivityOperation(entityName, DisplayActionName, activityClass)

  /** An Operation that will show the UI to the user for updating an entity instance. */
  def operationToShowUpdateUI(entityName: EntityName) =
    new StartEntityIdActivityOperation(entityName, UpdateActionName, activityClass)

  /** The command to undo the last delete. */
  lazy val commandToUndoDelete = Command(CommandId("undo_delete"), None, Some(res.R.string.undo_delete))

  /** A PortableField for modifying a named portion of a View. */
  def namedViewField[T](fieldName: String, childViewField: PortableField[T]): PortableField[T] = {
    viewId(rClass, fieldName, childViewField)
  }

  def listViewId(entityName: EntityName): Int = ViewRef(entityName + "_list", rClass, "id").viewKeyOrError

  def emptyListViewIdOpt(entityName: EntityName): Int = ViewRef(entityName + "_emptyList", rClass, "id").viewKeyOrError
}
