package com.github.scrud.action

import com.github.scrud.{UriPath, EntityType}
import com.github.scrud.persistence.CrudPersistence
import com.github.scrud.context.RequestContext

/** An operation that interacts with an entity's persistence.
  * The RequestContext is available as persistence.requestContext to implementing classes.
  * @author Eric Pabst (epabst@gmail.com)
  */
abstract class PersistenceOperation(entityType: EntityType) extends Operation {
  def invoke(uri: UriPath, persistence: CrudPersistence, requestContext: RequestContext)

  /** Runs the operation, given the uri and the current RequestContext. */
  def invoke(uri: UriPath, requestContext: RequestContext) {
    requestContext.sharedContext.withEntityPersistence(entityType) { persistence =>
      invoke(uri, persistence, requestContext)
    }
  }
}

