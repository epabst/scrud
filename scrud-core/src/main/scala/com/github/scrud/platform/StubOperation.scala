package com.github.scrud.platform

import com.github.scrud.{UriPath, EntityName}
import com.github.scrud.action.Operation
import com.github.scrud.context.CommandContext
import com.github.scrud.action.CrudOperationType.CrudOperationType

/**
 * A Crud Operation for use until a real one is implemented.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/1/14
 *         Time: 11:47 PM
 */
case class StubOperation(entityName: EntityName, operationType: CrudOperationType) extends Operation {
  /** Runs the operation, given the uri and the current CommandContext. */
  def invoke(uri: UriPath, commandContext: CommandContext) {
    println("Running Crud Operation: entityName=" + entityName + " operation=" + operationType)
  }
}
