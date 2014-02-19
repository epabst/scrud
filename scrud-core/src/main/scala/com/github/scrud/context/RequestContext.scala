package com.github.scrud.context

import com.github.scrud.UriPath
import com.github.scrud.state.State
import com.github.scrud.action.Undoable
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.action.CrudOperationType.CrudOperationType

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
  def operationType: CrudOperationType

  def uri: UriPath

  def sharedContext: SharedContext

  def entityTypeMap: EntityTypeMap = sharedContext.entityTypeMap

  def platformDriver: PlatformDriver = sharedContext.platformDriver

  /** The ISO 2 country such as "US". */
  def isoCountry: String

  private[context] val state: State = new State

  /** Provides a way for the user to undo an operation. */
  def allowUndo(undoable: Undoable)
}
