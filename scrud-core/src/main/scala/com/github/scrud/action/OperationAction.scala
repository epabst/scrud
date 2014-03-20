package com.github.scrud.action

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.UriPath
import com.github.scrud.context.RequestContext

/**
 * Represents an action that a user can initiate.
 * The Action and Operation should be stateless.
 * It's equals/hashCode MUST be implemented in order to suppress the action that is already happening.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/29/12
 * Time: 3:37 PM
 */
case class OperationAction(command: Command, operation: Operation) {
  def commandId: CommandNumber = command.commandNumber

  def invoke(uri: UriPath, requestContext: RequestContext) {
    operation.invoke(uri, requestContext)
  }

  /**
   * Creates a new Action that wraps this Action and does an additional operation.
   * It is patterned after [[scala.Function1.andThen]].
   */
  def andThen(nextOperation: Operation): OperationAction = OperationAction(command, operation.andThen(nextOperation))
}
