package com.github.scrud.context

import com.github.scrud.action.Undoable

/**
 * A RequestContext that simply wraps a SharedContext and can't be used for anything else.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/5/14
 *         Time: 12:34 AM
 */
class StubRequestContext(val sharedContext: SharedContext) extends RequestContext {
  private def notSupported: Nothing = throw new UnsupportedOperationException("StubRequestContext")

  /** Provides a way for the user to undo an operation. */
  def allowUndo(undoable: Undoable) = notSupported

  /** The ISO 2 country such as "US". */
  def isoCountry = notSupported

  def operationType = notSupported

  def uri = notSupported
}