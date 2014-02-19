package com.github.scrud.platform

import com.github.scrud.{UriPath, EntityName}
import com.github.scrud.action.Operation
import com.github.scrud.context.RequestContext
import com.github.scrud.action.CrudOperationType.CrudOperationType

/**
 * A CrudOperation for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/1/14
 *         Time: 11:47 PM
 */
case class CrudOperationForTesting(entityName: EntityName, operationType: CrudOperationType) extends Operation {
  /** Runs the operation, given the uri and the current RequestContext. */
  def invoke(uri: UriPath, requestContext: RequestContext) {
    println("Running Crud Operation: entityName=" + entityName + " operation=" + operationType)
  }
}
