package com.github.scrud.context

import com.github.scrud.{EntityNavigation, FieldDeclaration, UriPath, EntityName}
import com.github.scrud.state.{DestroyStateListener, State}
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.persistence.{PersistenceConnection, EntityTypeMap}
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.action.Undoable
import com.github.scrud.copy.{InstantiatingTargetType, SourceType}
import com.github.scrud.platform.representation.Persistence

/**
 * The context for a given interaction or command/response.
 * Some examples are:<ul>
 *   <li>An HTTP request/response</li>
 *   <li>An Android Fragment (or simple Activity)</li>
 * </ul>
 * It should have an action and a [[com.github.scrud.UriPath]],
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:25 PM
 */
trait CommandContext {
  @deprecated("this should be passed to the Action", since = "2014-03-19")
  protected def uri: UriPath

  def sharedContext: SharedContext

  def entityTypeMap: EntityTypeMap = sharedContext.entityTypeMap

  def platformDriver: PlatformDriver = sharedContext.platformDriver

  def entityNavigation: EntityNavigation

  lazy val persistenceConnection: PersistenceConnection = {
    val persistenceConnection = sharedContext.openPersistence()
    state.addListener(new DestroyStateListener {
      override def onDestroyState() {
        persistenceConnection.close()
      }
    })
    persistenceConnection
  }

  /** Find using this CommandContext's URI. */
  def find[V](entityName: EntityName, field: FieldDeclaration[V]): Option[V] =
    persistenceConnection.persistenceFor(entityName).find(uri).flatMap { entity =>
      field.toAdaptableField.sourceField(Persistence.Latest).findValue(entity, this)
    }

  /** Find using this CommandContext's URI. */
  def find[T <: AnyRef](entityName: EntityName, targetType: InstantiatingTargetType[T]): Option[T] =
    persistenceConnection.persistenceFor(entityName).find(uri, targetType, this)

  /** Find using this CommandContext's URI. */
  def findAll(entityName: EntityName): Seq[AnyRef] = persistenceConnection.persistenceFor(entityName).findAll(uri)

  /** Find using this CommandContext's URI. */
  def findAll[T <: AnyRef](entityName: EntityName, targetType: InstantiatingTargetType[T]): Seq[T] =
    persistenceConnection.persistenceFor(entityName).findAll[T](uri, targetType, this)

  def save(entityName: EntityName, sourceType: SourceType, idOpt: Option[ID], source: AnyRef): ID = {
    val persistence = persistenceConnection.persistenceFor(entityName)
    val dataToSave = entityTypeMap.entityType(entityName).copyAndUpdate(sourceType, source, persistence.targetType, persistence.newWritable(), this)
    persistence.save(idOpt, dataToSave)
  }

  /** The ISO 2 country such as "US". */
  def isoCountry: String

  private[context] val state: State = new State

  /** Provides a way for the user to undo an operation. */
  def allowUndo(undoable: Undoable)
}
