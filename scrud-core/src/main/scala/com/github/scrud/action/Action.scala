package com.github.scrud.action

import com.github.scrud.context.CommandContext
import com.github.scrud.view.ViewRequest

/**
 * An action, usually invoked by a user.
 * It should be stateless and reusable
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/19/14
 *         Time: 7:30 PM
 */
abstract class Action(actionKey: ActionKey) {
  /**
   * Invoke this Action and specify the view to render.
   * CommandContext has helpful methods for converting a simple [[com.github.scrud.view.ViewSpecifier]] or
   * [[com.github.scrud.view.ViewDataRequest]] into a [[com.github.scrud.view.ViewRequest]].
   * @param requestedCommand the command that resulted in this Action
   * @param commandContext the CommandContext, which includes platform-dependent support
   * @return a ViewRequest
   */
  def invoke(requestedCommand: Command, commandContext: CommandContext): ViewRequest
}
