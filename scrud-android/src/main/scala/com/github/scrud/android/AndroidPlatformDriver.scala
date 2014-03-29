package com.github.scrud.android

import com.github.scrud
import persistence.EntityTypePersistedInfo
import action.AndroidOperation._
import com.github.scrud.platform.PlatformDriver
import scrud.EntityType
import scrud.types._
import view.ViewField._
import view.FieldLayout._
import view.ViewRef
import com.github.triangle.PortableField
import com.github.scrud.copy._
import com.github.scrud.EntityName
import com.github.scrud.android.action.StartEntityActivityOperation
import com.github.scrud.android.view.EnumerationView
import com.github.scrud.action.ActionKey
import scala.Some
import com.github.scrud.android.action.StartEntityIdActivityOperation
import com.github.scrud.types.EnumerationValueQT
import com.github.scrud.android.view.EntityView
import com.github.scrud.action.PlatformCommand
import com.github.scrud.copy.FieldApplicability
import com.github.scrud.context.RequestContext
import com.github.scrud.platform.node.{MapTargetField, MapStorage}

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
  lazy val commandToUndoDelete = PlatformCommand(ActionKey("undo_delete"), None, Some(res.R.string.undo_delete))

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
      case CurrencyQT => currencyView
      case PercentageQT => percentageView
      case DateWithoutTimeQT => dateView
      case EnumerationValueQT(enumeration) => EnumerationView(enumeration)
      case e @ EntityName(_) => EntityView(e)
      case _: StringQualifiedType => textView
      case _: IntQualifiedType => intView.withDefaultLayout(textLayout("number"))
    }).asInstanceOf[PortableField[T]]
    namedViewField(fieldName, childViewField, entityName)
  }

  def listViewId(entityName: EntityName): Int = ViewRef(entityName + "_list", rClass, "id").viewKeyOrError

  def emptyListViewIdOpt(entityName: EntityName): Int = ViewRef(entityName + "_emptyList", rClass, "id").viewKeyOrError

  def field[V](fieldName: String, qualifiedType: QualifiedType[V], applicability: FieldApplicability, entityName: EntityName): AdaptableField[V] = {
    val sourceFields: Map[SourceType,SourceField[V]] = (for {
      sourceType <- applicability.from
      tuple <- makeSourceFields(fieldName, qualifiedType, sourceType, entityName)
    } yield tuple).toMap

    val targetFields: Map[TargetType,TargetField[V]] = (for {
      targetType <- applicability.to
    } yield (targetType, makeTargetField(fieldName, qualifiedType, targetType, entityName))).toMap

    new AdaptableFieldByType[V](sourceFields, targetFields)
  }

  protected def makeSourceFields[V](fieldName: String, qualifiedType: QualifiedType[V], sourceType: SourceType, entityName: EntityName): Map[SourceType,SourceField[V]] = {
    sourceType match {
      case MapStorage =>
        val field = TypedSourceField[MapStorage, V](_.get(entityName, fieldName).map(_.asInstanceOf[V]))
        Map(sourceType -> field)
      case _ =>
        val field = new SourceField[V] {
          def findValue(source: AnyRef, context: RequestContext) = None //todo
        }
        Map(sourceType -> field)
    }
  }

  protected def makeTargetField[V](fieldName: String, qualifiedType: QualifiedType[V], targetType: TargetType, entityName: EntityName): TargetField[V] = {
    targetType match {
      case MapStorage =>
        new MapTargetField[V](entityName, fieldName)
      case _ =>
        new TargetField[V] {
          def putValue(target: AnyRef, valueOpt: Option[V], context: RequestContext) = {
            //todo
          }
        }
    }
  }
}

object AndroidPlatformDriver {
  def fieldPrefix(entityName: EntityName): String = entityName.toString + "_"
}
