package com.github.scrud.action

import com.github.scrud.UriPath
import com.github.scrud.context.RequestContext

/**
 * Represents an operation that a user can initiate.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/29/12
 * Time: 3:32 PM
 */
trait Operation { self =>
  /** Runs the operation, given the uri and the current RequestContext. */
  def invoke(uri: UriPath, requestContext: RequestContext)

  /**
   * Creates a new Operation that wraps this Operation and does an additional operation.
   * It is patterned after [[scala.Function1.andThen]].
   */
  def andThen(nextOperation: Operation): Operation = new Operation {
    /** Runs the operation, given the uri and the current RequestContext. */
    def invoke(uri: UriPath, requestContext: RequestContext) {
      self.invoke(uri, requestContext)
      nextOperation.invoke(uri, requestContext)
    }
  }
}
