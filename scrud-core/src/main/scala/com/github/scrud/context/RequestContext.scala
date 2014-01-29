package com.github.scrud.context

import com.github.scrud.UriPath
import com.github.scrud.state.State
import com.github.scrud.action.{Undoable, CrudOperationType}
import com.github.scrud.platform.PlatformDriver

/**
 * The context for a given interaction or request/response.
 * Some examples are:<ul>
 *   <li>An HTTP request/response</li>
 *   <li>An Android Fragment (or simple Activity)</li>
 * </ul>
 * It should have an action and a [[com.github.scrud.UriPath]],
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:25 PM
 */
trait RequestContext {
  def operationType: CrudOperationType.Value

  def uri: UriPath

  def sharedContext: SharedContext

  def platformDriver: PlatformDriver = sharedContext.platformDriver

  private[context] val state: State = new State

  /** Provides a way for the user to undo an operation. */
  def allowUndo(undoable: Undoable)
}
