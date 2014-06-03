package com.github.scrud.context

import com.github.scrud.action.Undoable
import com.github.scrud.platform.PlatformTypes

/**
 * A CommandContext that simply wraps a SharedContext and can't be used for anything else.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/5/14
 *         Time: 12:34 AM
 */
class StubCommandContext(val sharedContext: SharedContext) extends CommandContext {
  private def notSupported: Nothing = throw new UnsupportedOperationException("StubCommandContext")

  def entityNavigation: Nothing = throw new UnsupportedOperationException("StubCommandContext")

  def displayMessageToUser(message: String) = throw new UnsupportedOperationException("StubCommandContext")

  def displayMessageToUserBriefly(messageKey: PlatformTypes.SKey) = throw new UnsupportedOperationException("StubCommandContext")

  /** Provides a way for the user to undo an operation. */
  def allowUndo(undoable: Undoable) = notSupported

  /** The ISO 2 country such as "US". */
  def isoCountry = notSupported
}
