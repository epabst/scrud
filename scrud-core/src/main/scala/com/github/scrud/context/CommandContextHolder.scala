package com.github.scrud.context

import com.github.scrud._
import com.github.scrud.persistence.{CrudPersistence, PersistenceConnection}
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.copy._
import com.github.scrud.platform.Notification
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.scrud.EntityName
import com.github.scrud.action.Undoable
import com.github.scrud.FieldDeclaration
import com.github.scrud.copy.types.ValidationResult

/**
 * The context for a given interaction or command/response.
 * Some examples are:<ul>
 *   <li>An HTTP request/response</li>
 *   <li>An Android Fragment (or simple Activity)</li>
 * </ul>
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:25 PM
 */
//This is private so that subclasses CommandContext or CommandContextDelegator will be used instead.
private[context] trait CommandContextHolder extends SharedContextHolder with Notification {
  private lazy val dataSavedNotificationStringKey = platformDriver.getStringKey("data_saved_notification")
  private lazy val dataNotSavedSinceInvalidNotificationStringKey = platformDriver.getStringKey("data_not_saved_since_invalid_notification")

  protected def commandContext: CommandContext

  def entityNavigation: EntityNavigation

  def future[T](body: => T): Future[T] = Future(body)

  def persistenceConnection: PersistenceConnection

  def persistenceFor(entityType: EntityType): CrudPersistence = persistenceConnection.persistenceFor(entityType)

  def persistenceFor(entityName: EntityName): CrudPersistence = persistenceFor(entityTypeMap.entityType(entityName))

  def persistenceFor(uri: UriPath): CrudPersistence = persistenceFor(UriPath.lastEntityNameOrFail(uri))

  def findDefault(entityType: EntityType, uri: UriPath, targetType: TargetType): AdaptedValueSeq = {
    entityType.copy(SourceType.none, SourceType.none, uri, targetType, commandContext)
  }

  def findDefault[V](field: FieldDeclaration[V], sourceUri: UriPath): Option[V] =
    field.toAdaptableField.findFromContext(sourceUri, commandContext)

  /** Find the field value for a certain entity by ID. */
  def find[V](entityName: EntityName, id: ID, field: FieldDeclaration[V]): Option[V] = {
    val persistence = persistenceFor(entityName)
    val sourceUri = entityName.toUri(id)
    persistence.find(sourceUri).flatMap { entity =>
      field.toAdaptableField.sourceFieldOrFail(persistence.sourceType).findValue(entity, new CopyContext(sourceUri, commandContext))
    }
  }

  /** Find using this CommandContext's URI. */
  def findOrElseDefault[V](uri: UriPath, field: FieldDeclaration[V]): Option[V] =
    find(uri, field).orElse(findDefault(field, uri))

  /** Find the non-empty field values for all entities matching a URI. */
  def findAll[V](uri: UriPath, field: FieldDeclaration[V]): Seq[V] = {
    val persistence = persistenceFor(uri)
    val sourceField = field.toAdaptableField.sourceFieldOrFail(persistence.sourceType)
    val copyContext = new CopyContext(uri, commandContext)
    persistence.findAll(uri).flatMap { entity =>
      sourceField.findValue(entity, copyContext)
    }
  }

  /** Find the field value for a certain entity by URI. */
  def find[V](uri: UriPath, field: FieldDeclaration[V]): Option[V] = {
    val entityName = UriPath.lastEntityNameOrFail(uri)
    UriPath.findId(uri, entityName).flatMap(find(entityName, _, field))
  }

  /** Find a certain entity by URI and copy it to the targetType. */
  def find[T <: AnyRef](uri: UriPath, targetType: InstantiatingTargetType[T]): Option[T] = {
    persistenceFor(uri).find(uri, targetType, commandContext)
  }

  /** Find all a certain entity by URI and copy them to the targetType. */
  def findAll[T <: AnyRef](uri: UriPath, targetType: InstantiatingTargetType[T]): Seq[T] =
    persistenceFor(uri).findAll[T](uri, targetType, commandContext)

  /** Find all a certain entity by EntityName and copy them to the targetType. */
  def findAll[T <: AnyRef](entityName: EntityName, targetType: InstantiatingTargetType[T]): Seq[T] =
    findAll(entityName.toUri, targetType)

  def delete(uri: UriPath): Int = persistenceFor(uri).delete(uri)

  def save(entityName: EntityName, sourceType: SourceType, idOpt: Option[ID], source: AnyRef): ID = {
    val sourceUri = entityName.toUri(idOpt)
    val persistence = persistenceFor(entityName)
    val dataToSave = entityTypeMap.entityType(entityName).copyAndUpdate(sourceType, source, sourceUri,
      persistence.targetType, persistence.newWritable(), commandContext)
    persistence.save(idOpt, dataToSave)
  }

  /**
   * Save the data into the persistence for entityType.
   * If data is invalid (based on updating a ValidationResult), returns None, otherwise returns the created or updated ID.
   */
  def saveIfValid(sourceUri: UriPath, sourceType: SourceType, source: AnyRef, entityType: EntityType): Option[ID] = {
    val idSourceField = entityType.idField.sourceFieldOrFail(sourceType)
    val copyContext = new CopyContext(sourceUri, commandContext)
    val idOpt = idSourceField.findValue(source, copyContext)
    if (entityType.copyAndUpdate(sourceType, source, sourceUri, ValidationResult, commandContext).isValid) {
      val newId = commandContext.save(entityType.entityName, sourceType, idOpt, source)
      commandContext.displayMessageToUserBriefly(dataSavedNotificationStringKey)
      Some(newId)
    } else {
      commandContext.displayMessageToUserBriefly(dataNotSavedSinceInvalidNotificationStringKey)
      None
    }
  }

  /** The ISO 2 country such as "US". */
  def isoCountry: String

  /** Provides a way for the user to undo an operation. */
  def allowUndo(undoable: Undoable)
}
