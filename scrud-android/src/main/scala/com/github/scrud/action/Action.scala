package com.github.scrud.action

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.{CrudContext, UriPath}
import com.github.scrud.android.action.Command

/**
 * Represents an action that a user can initiate.
 * It's equals/hashCode MUST be implemented in order to suppress the action that is already happening.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/29/12
 * Time: 3:37 PM
 */
case class Action(command: Command, operation: Operation) {
  def commandId: CommandId = command.commandId

  def invoke(uri: UriPath, crudContext: CrudContext) {
    operation.invoke(uri, crudContext)
  }
}
