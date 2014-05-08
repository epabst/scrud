package com.github.scrud.context

import com.github.scrud.{EntityNavigation, FieldDeclaration, UriPath, EntityName}
import com.github.scrud.state.{DestroyStateListener, State}
import com.github.scrud.persistence.PersistenceConnection
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
trait CommandContext extends SharedContextHolder {
  //todo this should probably be passed with each invocation rather than here.
  protected def uri: UriPath

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

  def findDefault[V](field: FieldDeclaration[V]): Option[V] = field.toAdaptableField.findDefault(this)

  /** Find using this CommandContext's URI. */
  def find[V](uri: UriPath, field: FieldDeclaration[V]): Option[V] =
    persistenceConnection.find(uri, field, this).orElse(findDefault(field))

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
