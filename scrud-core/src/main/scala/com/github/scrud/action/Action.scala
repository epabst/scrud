package com.github.scrud.action

import com.github.scrud.context.CommandContext
import com.github.scrud.view.ViewSpecifier

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
   * @param requestedCommand the command that resulted in this Action
   * @param commandContext the CommandContext, which includes platform-dependent support
   * @return a ViewSpecifier
   */
  def invoke(requestedCommand: Command, commandContext: CommandContext): ViewSpecifier
}
