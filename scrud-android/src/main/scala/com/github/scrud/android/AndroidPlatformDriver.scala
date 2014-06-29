package com.github.scrud.android

import action.AndroidOperation._
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.android.view.{AndroidEditUIFieldFactory, AndroidDisplayUIFieldFactory, ViewRef}
import com.github.scrud.action.ActionKey
import com.github.scrud.android.persistence.{ContentValuesStorageAdaptableFieldFactory, QueryAdaptableFieldFactory, SQLiteAdaptableFieldFactory}
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.android.view.AndroidResourceAnalyzer._
import com.github.scrud.FieldName
import com.github.scrud.EntityName
import com.github.scrud.android.action.StartEntityActivityOperation
import com.github.scrud.android.view.ViewSpecifier
import scala.Some
import com.github.scrud.action.PlatformCommand
import com.github.scrud.android.action.StartEntityIdActivityOperation
import scala.util.Try
import android.R
import com.github.scrud.util.Name
import android.provider.BaseColumns
import com.netaporter.uri.Uri

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

  private lazy val deleteItemStringKey = getStringKey("delete_item")

  def commandToListItems(entityName: EntityName): PlatformCommand = PlatformCommand(ActionKey(entityName.toSnakeCase + "_list"), None,
    tryStringKey(entityName.toSnakeCase + "_list").toOption)

  def commandToDisplayItem(entityName: EntityName): PlatformCommand = PlatformCommand(ActionKey("display_" + entityName.toSnakeCase),
    None, None)

  def commandToAddItem(entityName: EntityName): PlatformCommand = PlatformCommand(ActionKey("add_" + entityName.toSnakeCase),
    Some(R.drawable.ic_menu_add),
    Some(getStringKey("add_" + entityName.toSnakeCase)))

  def commandToEditItem(entityName: EntityName): PlatformCommand = PlatformCommand(ActionKey("edit_" + entityName.toSnakeCase),
    Some(R.drawable.ic_menu_edit), Some(getStringKey("edit_" + entityName.toSnakeCase)))

  def commandToDeleteItem(entityName: EntityName): PlatformCommand = {
    PlatformCommand(ActionKey("delete_" + entityName.toSnakeCase),
      Some(R.drawable.ic_menu_delete), Some(deleteItemStringKey))
  }

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
    ContentValuesStorageAdaptableFieldFactory,
    BundleStorageAdaptableFieldFactory,
    new AndroidDisplayUIFieldFactory(this),
    new AndroidEditUIFieldFactory(this),
    SQLiteAdaptableFieldFactory,
    QueryAdaptableFieldFactory
  )

  def toViewSpecifier(fieldPrefix: String, fieldName: FieldName): ViewSpecifier =
    new ViewSpecifier(toViewRef(fieldPrefix, fieldName))

  def toViewRef(fieldPrefix: String, fieldName: FieldName): ViewRef = {
    val viewKey = fieldPrefix + fieldName
    ViewRef(viewKey, rClass, "id")
  }

  def listViewId(entityName: EntityName): Int = ViewRef(entityName + "_list", rClass, "id").viewKeyOrError

  def emptyListViewIdOpt(entityName: EntityName): Int = ViewRef(entityName + "_emptyList", rClass, "id").viewKeyOrError

  private lazy val classInApplicationPackage: Class[_] = rClass
  lazy val rStringClasses: Seq[Class[_]] = detectRStringClasses(classInApplicationPackage)
  lazy val rDrawableClasses: Seq[Class[_]] = detectRDrawableClasses(classInApplicationPackage)

  def tryStringKey(stringName: String): Try[SKey] = Try {
    findResourceIdWithName(rStringClasses, stringName).getOrElse {
//      rStringClasses.foreach(rStringClass => logError("Contents of " + rStringClass + " are " + rStringClass.getFields.mkString(", ")))
      val message = "R.string." + stringName + " not found.  You may want to run the CrudUIGenerator.generateLayouts." +
        rStringClasses.mkString("(string classes: ", ",", ")")
      throw new IllegalStateException(message)
    }
  }

  def tryBinaryResource(resourceName: Name): Try[Uri] = Try {
    val stringName: String = resourceName.toSnakeCase
    val resourceId = findResourceIdWithName(rStringClasses, stringName).getOrElse {
      rStringClasses.foreach(rStringClass => logError("Contents of " + rStringClass + " are " + rStringClass.getFields.mkString(", ")))
      throw new IllegalStateException("R.string." + stringName + " not found.  You may want to run the CrudUIGenerator.generateLayouts." +
        rStringClasses.mkString("(string classes: ", ",", ")"))
    }
    Uri.parse("android.resource://" + rClass.getPackage.getName + "/" + resourceId)
  }

  def tryImageKey(imageName: Name): Try[ImgKey] = Try {
    findResourceIdWithName(rDrawableClasses, imageName.toSnakeCase).getOrElse {
      rDrawableClasses.foreach(rDrawableClass => logError("Contents of " + rDrawableClass + " are " + rDrawableClass.getFields.mkString(", ")))
      throw new IllegalStateException("R.drawable." + imageName + " not found." +
        rDrawableClasses.mkString("(drawable classes: ", ",", ")"))
    }
  }

  lazy val rLayoutClasses: Seq[Class[_]] = detectRLayoutClasses(classInApplicationPackage)

  /** The layout used for each entity when allowing the user to pick one of them. */
  def selectUILayoutFor(entityName: EntityName): LayoutKey = {
    findResourceIdWithName(rLayoutClasses, entityName.toSnakeCase + "_pick").getOrElse(
      _root_.android.R.layout.simple_spinner_dropdown_item)
  }

  def getLayoutKey(layoutName: String): LayoutKey =
    findResourceIdWithName(rLayoutClasses, layoutName).getOrElse {
      rLayoutClasses.foreach(layoutClass => logError("Contents of " + layoutClass + " are " + layoutClass.getFields.mkString(", ")))
      throw new IllegalStateException("R.layout." + layoutName + " not found.  You may want to run the CrudUIGenerator.generateLayouts." +
        rLayoutClasses.mkString("(layout classes: ", ",", ")"))
    }

  /**
   * Gets the name of a field that contains the entity's ID.
   * @param entityName the entity whose id field is needed
   * @return the name of the field
   * @see [[com.github.scrud.EntityType.idField]]
   */
  override def idFieldName(entityName: EntityName): String = BaseColumns._ID
}
