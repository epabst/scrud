package com.github.scrud.action

import com.github.scrud.UriPath
import com.github.scrud.persistence.PersistenceConnection
import com.github.scrud.context.CommandContext

/** An operation that interacts with an entity's persistence.
  * The CommandContext is available as persistenceConnection.commandContext to implementing classes.
  * @author Eric Pabst (epabst@gmail.com)
  */
abstract class PersistenceOperation extends Operation {
  def invoke(uri: UriPath, persistenceConnection: PersistenceConnection)

  /** Runs the operation, given the uri and the current CommandContext. */
  def invoke(uri: UriPath, commandContext: CommandContext) {
    invoke(uri, commandContext.persistenceConnection)
  }
}

