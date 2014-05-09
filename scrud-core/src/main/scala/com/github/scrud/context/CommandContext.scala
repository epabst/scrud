package com.github.scrud.context

import com.github.scrud.{UriPath, EntityName}
import com.github.scrud.state.{DestroyStateListener, State}
import com.github.scrud.persistence.PersistenceConnection
import com.github.scrud.copy.InstantiatingTargetType

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
trait CommandContext extends CommandContextHolder {
  protected def commandContext: CommandContext = this

  //todo this should probably be passed with each invocation rather than here.
  protected def uri: UriPath

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
  def findAll(entityName: EntityName): Seq[AnyRef] = persistenceConnection.persistenceFor(entityName).findAll(uri)

  /** Find using this CommandContext's URI. */
  def findAll[T <: AnyRef](entityName: EntityName, targetType: InstantiatingTargetType[T]): Seq[T] =
    persistenceConnection.findAll(uri, targetType, this)

  private[context] val state: State = new State
}
