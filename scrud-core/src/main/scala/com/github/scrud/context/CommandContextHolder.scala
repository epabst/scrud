package com.github.scrud.context

import com.github.scrud._
import com.github.scrud.persistence.PersistenceConnection
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.copy._
import com.github.scrud.platform.Notification
import scala.concurrent.Future
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

  def future[T](body: => T): Future[T] = sharedContext.future(body)

  def persistenceConnection: PersistenceConnection

  def findDefault(entityType: EntityType, uri: UriPath, targetType: TargetType): AdaptedValueSeq = {
    entityType.copy(SourceType.none, SourceType.none, uri, targetType, commandContext)
  }

  def findDefault[T <: AnyRef](entityType: EntityType, uri: UriPath, targetType: TargetType, target: T): T =
    entityType.copyAndUpdate(SourceType.none, SourceType.none, uri, targetType, target, commandContext)

  def findDefault[V](field: FieldDeclaration[V], sourceUri: UriPath): Option[V] =
    field.toAdaptableField.findFromContext(sourceUri, commandContext)

  /**
   * Save the data into the persistence for entityType.
   * If data is invalid (based on updating a ValidationResult), returns None, otherwise returns the created or updated ID.
   */
  def saveIfValid(sourceUri: UriPath, sourceType: SourceType, source: AnyRef, entityType: EntityType): Option[ID] = {
    val idSourceField = entityType.idField.sourceFieldOrFail(sourceType)
    val copyContext = new CopyContext(sourceUri, commandContext)
    val idOpt = idSourceField.findValue(source, copyContext)
    if (entityType.copyAndUpdate(sourceType, source, sourceUri, ValidationResult, commandContext).isValid) {
      val newId = commandContext.save(entityType.entityName, idOpt, sourceType, source)
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
