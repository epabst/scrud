package com.github.scrud.action

import com.github.scrud.{CrudContext, UriPath}

/**
 * Represents an operation that a user can initiate.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/29/12
 * Time: 3:32 PM
 */
trait Operation { self =>
  /** Runs the operation, given the uri and the current CrudContext. */
  def invoke(uri: UriPath, crudContext: CrudContext)

  /**
   * Creates a new Operation that wraps this Operation and does an additional operation.
   * It is patterned after [[scala.Function1.andThen]].
   */
  def andThen(nextOperation: Operation): Operation = new Operation {
    /** Runs the operation, given the uri and the current CrudContext. */
    def invoke(uri: UriPath, crudContext: CrudContext) {
      self.invoke(uri, crudContext)
      nextOperation.invoke(uri, crudContext)
    }
  }
}
