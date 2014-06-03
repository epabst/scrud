package com.github.scrud.android

import action.AndroidOperation._
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.android.view.{AndroidEditUIFieldFactory, AndroidDisplayUIFieldFactory, ViewRef}
import com.github.scrud.action.ActionKey
import com.github.scrud.android.persistence.{QueryAdaptableFieldFactory, SQLiteAdaptableFieldFactory}
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.android.view.AndroidResourceAnalyzer._
import com.github.scrud.FieldName
import com.github.scrud.EntityName
import com.github.scrud.android.action.StartEntityActivityOperation
import com.github.scrud.android.view.ViewSpecifier
import scala.Some
import com.github.scrud.action.PlatformCommand
import com.github.scrud.android.action.StartEntityIdActivityOperation

/**
 * A PlatformDriver for the Android platform.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 8/28/12
 * Time: 10:23 AM
 * @param rClass classOf[R] from the application's package
 * @param activityClass classOf[CrudActivity] or a custom subclass for the application
 */
class AndroidPlatformDriver(rClass: Class[_], val activityClass: Class[_ <: CrudActivity] = classOf[CrudActivity])
    extends PlatformDriver {
  lazy val localDatabasePersistenceFactory = new SQLitePersistenceFactory

  lazy val undoDeleteStringKey = getStringKey("undo_delete")
  /** The command to undo the last delete. */
  lazy val commandToUndoDelete = PlatformCommand(ActionKey("undo_delete"), None, Some(undoDeleteStringKey))

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

  val platformSpecificFieldFactories = Seq(
    new AndroidDisplayUIFieldFactory(this),
    new AndroidEditUIFieldFactory(this),
    SQLiteAdaptableFieldFactory,
    BundleStorageAdaptableFieldFactory,
    QueryAdaptableFieldFactory
  )

  def toViewSpecifier(entityName: EntityName, fieldPrefix: String, fieldName: FieldName): ViewSpecifier =
    new ViewSpecifier(toViewRef(entityName, fieldPrefix, fieldName))

  def toViewRef(entityName: EntityName, fieldPrefix: String, fieldName: FieldName): ViewRef = {
    val viewKey = AndroidPlatformDriver.fieldPrefix(entityName) + fieldPrefix + fieldName
    ViewRef(viewKey, rClass, "id")
  }

  def listViewId(entityName: EntityName): Int = ViewRef(entityName + "_list", rClass, "id").viewKeyOrError

  def emptyListViewIdOpt(entityName: EntityName): Int = ViewRef(entityName + "_emptyList", rClass, "id").viewKeyOrError

  private lazy val classInApplicationPackage: Class[_] = rClass
  lazy val rStringClasses: Seq[Class[_]] = detectRStringClasses(classInApplicationPackage)

  def getStringKey(stringName: String): SKey =
    findStringKey(stringName).getOrElse {
      rStringClasses.foreach(rStringClass => logError("Contents of " + rStringClass + " are " + rStringClass.getFields.mkString(", ")))
      throw new IllegalStateException("R.string." + stringName + " not found.  You may want to run the CrudUIGenerator.generateLayouts." +
        rStringClasses.mkString("(string classes: ", ",", ")"))
    }

  def findStringKey(stringName: String): Option[SKey] = findResourceIdWithName(rStringClasses, stringName)
}

object AndroidPlatformDriver {
  def fieldPrefix(entityName: EntityName): String = entityName.toString + "_"
}
