package com.github.scrud.action

import com.github.scrud.context.RequestContext

/**
 * An action, usually invoked by a user.
 * It should be stateless and reusable
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/19/14
 *         Time: 7:30 PM
 */
abstract class Action(commandKey: CommandKey) {
  def invoke(requestedCommand: Command, requestContext: RequestContext): Response
}
