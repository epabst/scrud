package com.github.scrud.platform

import com.github.scrud.persistence.ListBufferPersistenceFactory
import com.github.scrud.{EntityType, CrudContext, UriPath, EntityName}
import com.github.scrud.action.{Operation, CrudOperationType}
import com.github.triangle.{Updater, Getter, PortableField}
import com.github.scrud.view.NamedViewMap
import com.github.scrud.types.QualifiedType
import com.github.scrud.copy._
import com.github.scrud.EntityName
import com.github.scrud.action.CommandId
import com.github.scrud.action.Command
import com.github.scrud.copy.FieldApplicability
import com.github.scrud.context.RequestContext

/**
 * A simple PlatformDriver for testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/28/12
 *         Time: 1:27 PM
 */
class TestingPlatformDriver extends PlatformDriver {
  protected def logTag = getClass.getSimpleName

  val localDatabasePersistenceFactory = new ListBufferPersistenceFactory[AnyRef](Map.empty[String,Any])

  def calculateDataVersion(entityTypes: Seq[EntityType]) = 1

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

  /** The command to undo the last delete. */
  def commandToUndoDelete = Command(CommandId("command1"), None, None)

  /** A PortableField for modifying a named portion of a View. */
  def namedViewField[T](fieldName: String, childViewField: PortableField[T], entityName: EntityName) =
    namedViewField(fieldName, entityName)

  /**
   * A PortableField for modifying a named portion of a View.
   * The platform is expected to recognize the qualifiedType and be able to return a PortableField.
   * @throws IllegalArgumentException if the qualifiedType is not recognized.
   */
  def namedViewField[T](fieldName: String, qualifiedType: QualifiedType[T], entityName: EntityName) =
    namedViewField(fieldName, entityName)

  /** A PortableField for modifying a named portion of a View. */
  def namedViewField[T](fieldName: String, entityName: EntityName): PortableField[T] =
    namedViewField(entityName.toString + "_" + fieldName)

  /** A PortableField for modifying a named portion of a View. */
  def namedViewField[T](fieldName: String): PortableField[T] = {
    Getter.single[T]({
      case map: NamedViewMap if map.contains(fieldName) =>  map.apply(fieldName).asInstanceOf[Option[T]]
    }) + Updater((m: NamedViewMap) => (valueOpt: Option[T]) => m + (fieldName -> valueOpt))
  }

  def field[V](fieldName: String, qualifiedType: QualifiedType[V], applicability: FieldApplicability, entityName: EntityName): AdaptableField[V] = {
    val someSourceField = Some(new TypedSourceField[MapStorage,V] {
      def findValue(from: MapStorage, context: RequestContext) = {
        from.get(entityName, fieldName).map(_.asInstanceOf[V])
      }
    })
    val someTargetField = Some(new TypedTargetField[MapStorage,V] {
      /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
      def putValue(target: MapStorage, valueOpt: Option[V], context: RequestContext) = {
        target.put(entityName, fieldName, valueOpt)
      }
    })
    new AdaptableField[V] {
      def findSourceField(sourceType: SourceType) =
        if (applicability.contains(sourceType)) {
          someSourceField
        } else {
          None
        }

      def findTargetField(targetType: TargetType) = {
        if (applicability.contains(targetType)) {
          someTargetField
        } else {
          None
        }
      }
    }
  }
}

object TestingPlatformDriver extends TestingPlatformDriver

case class ShowEntityUIOperationForTesting(entityName: EntityName, operationType: CrudOperationType.Value) extends Operation {
  /** Runs the operation, given the uri and the current CrudContext. */
  def invoke(uri: UriPath, crudContext: CrudContext) {
    println("Showing Entity UI Operation: entityName=" + entityName + " operation=" + operationType)
  }
}
