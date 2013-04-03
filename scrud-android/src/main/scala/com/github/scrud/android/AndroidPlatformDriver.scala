package com.github.scrud.android

import com.github.scrud
import persistence.EntityTypePersistedInfo
import scrud.action.{CommandId, Command}
import scrud.android.action.{StartEntityIdActivityOperation, StartEntityActivityOperation}
import action.AndroidOperation._
import com.github.scrud.platform.PlatformDriver
import scrud.{EntityType, EntityName}
import scrud.types._
import view.ViewField._
import view.{EntityView, EnumerationView, ViewRef}
import view.FieldLayout._
import scala.Some
import com.github.triangle.PortableField

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

  def calculateDataVersion(entityTypes: Seq[EntityType]) = entityTypes.map(EntityTypePersistedInfo(_).maxDataVersion).max

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
  def namedViewField[T](fieldName: String, childViewField: PortableField[T], entityName: EntityName): PortableField[T] = {
    viewId(rClass, AndroidPlatformDriver.fieldPrefix(entityName) + fieldName, childViewField)
  }

  /**
   * A PortableField for modifying a named portion of a View.
   * The platform is expected to recognize the qualifiedType and be able to return a PortableField.
   * @throws IllegalArgumentException if the qualifiedType is not recognized.
   */
  def namedViewField[T](fieldName: String, qualifiedType: QualifiedType[T], entityName: EntityName) = {
    val childViewField = (qualifiedType match {
      case TitleQT => textView.withDefaultLayout(textLayout("text|textCapWords|textAutoCorrect"))
      case DescriptionQT => textView.withDefaultLayout(textLayout("text|textCapSentences|textMultiLine|textAutoCorrect"))
      case NaturalIntQT | PositiveIntQT => intView.withDefaultLayout(textLayout("number"))
      case CurrencyQT => currencyView
      case PercentageQT => percentageView
      case DateWithoutTimeQT => dateView
      case EnumerationValueQT(enumeration) => EnumerationView(enumeration)
      case e @ EntityName(_) => EntityView(e)
    }).asInstanceOf[PortableField[T]]
    namedViewField(fieldName, childViewField, entityName)
  }

  def listViewId(entityName: EntityName): Int = ViewRef(entityName + "_list", rClass, "id").viewKeyOrError

  def emptyListViewIdOpt(entityName: EntityName): Int = ViewRef(entityName + "_emptyList", rClass, "id").viewKeyOrError
}

object AndroidPlatformDriver {
  def fieldPrefix(entityName: EntityName): String = entityName.toString + "_"
}