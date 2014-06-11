package com.github.scrud.context

import com.github.scrud.state.{DestroyStateListener, State}
import com.github.scrud.persistence.PersistenceConnection

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

  override def persistenceConnection: PersistenceConnection = persistenceConnectionVal

  protected lazy val persistenceConnectionVal: PersistenceConnection = {
    val persistenceConnection = new PersistenceConnection(this)
    state.addListener(new DestroyStateListener {
      override def onDestroyState() {
        persistenceConnection.close()
      }
    })
    persistenceConnection
  }

  private[context] val state: State = new State
}
