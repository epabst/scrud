package com.github.scrud.action

import com.github.scrud.UriPath
import com.github.scrud.persistence.PersistenceConnection
import com.github.scrud.context.RequestContext

/** An operation that interacts with an entity's persistence.
  * The RequestContext is available as persistence.requestContext to implementing classes.
  * @author Eric Pabst (epabst@gmail.com)
  */
abstract class PersistenceOperation extends Operation {
  def invoke(uri: UriPath, persistenceConnection: PersistenceConnection, requestContext: RequestContext)

  /** Runs the operation, given the uri and the current RequestContext. */
  def invoke(uri: UriPath, requestContext: RequestContext) {
    requestContext.sharedContext.withPersistence { persistenceConnection =>
      invoke(uri, persistenceConnection, requestContext)
    }
  }
}

