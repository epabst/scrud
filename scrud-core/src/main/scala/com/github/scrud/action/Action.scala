package com.github.scrud.action

import com.netaporter.uri.Uri
import com.github.scrud.copy.SourceType
import com.github.scrud.context.RequestContext

/**
 * An action, usually invoked by a user.
 * It should be stateless and reusable
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/19/14
 *         Time: 7:30 PM
 */
abstract class Action(commandKey: CommandKey) {
  def invoke(request: Request, requestContext: RequestContext): Response
}
