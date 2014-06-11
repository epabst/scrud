package com.github.scrud.action

import com.github.scrud.UriPath
import com.github.scrud.context.CommandContext

/**
 * Represents an operation that a user can initiate.
 * It should be totally stateless.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/29/12
 * Time: 3:32 PM
 */
trait Operation { self =>
  /** Runs the operation, given the uri and the current CommandContext. */
  def invoke(uri: UriPath, commandContext: CommandContext)

  /**
   * Creates a new Operation that wraps this Operation and does an additional operation.
   * It is patterned after [[scala.Function1.andThen]].
   */
  def andThen(nextOperation: Operation): Operation = new Operation {
    /** Runs the operation, given the uri and the current CommandContext. */
    def invoke(uri: UriPath, commandContext: CommandContext) {
      self.invoke(uri, commandContext)
      nextOperation.invoke(uri, commandContext)
    }
  }
}
