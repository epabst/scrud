package com.github.scrud.context

import com.github.scrud.{EntityNavigation, FieldDeclaration, UriPath, EntityName}
import com.github.scrud.state.{DestroyStateListener, State}
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.persistence.{PersistenceConnection, EntityTypeMap}
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.action.Undoable
import com.github.scrud.copy.{InstantiatingTargetType, SourceType}

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
  //todo this should probably be passed with each invocation rather than here.
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
    UriPath.findId(uri, entityName).flatMap(persistenceConnection.find(entityName, _, field, this))

  /** Find using this CommandContext's URI. */
  def find[T <: AnyRef](entityName: EntityName, targetType: InstantiatingTargetType[T]): Option[T] =
    persistenceConnection.find(uri, targetType, this)

  /** Find using this CommandContext's URI. */
  def findAll(entityName: EntityName): Seq[AnyRef] = persistenceConnection.persistenceFor(entityName).findAll(uri)

  /** Find using this CommandContext's URI. */
  def findAll[T <: AnyRef](entityName: EntityName, targetType: InstantiatingTargetType[T]): Seq[T] =
    persistenceConnection.findAll(uri, targetType, this)

  def save(entityName: EntityName, sourceType: SourceType, idOpt: Option[ID], source: AnyRef): ID =
    persistenceConnection.save(entityName, sourceType, idOpt, source, this)

  /** The ISO 2 country such as "US". */
  def isoCountry: String

  private[context] val state: State = new State

  /** Provides a way for the user to undo an operation. */
  def allowUndo(undoable: Undoable)
}
