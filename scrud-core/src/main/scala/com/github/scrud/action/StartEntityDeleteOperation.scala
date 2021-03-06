package com.github.scrud.action

import com.github.scrud.{UriPath, EntityType}
import com.github.scrud.persistence.PersistenceConnection
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.platform.representation.Persistence

/**
 * Delete an entity by Uri with an undo option.  It can be wrapped to do a confirmation box if desired.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 11/29/12
 * Time: 7:51 AM
 */
//final to guarantee equality is correct
final case class StartEntityDeleteOperation(entityType: EntityType) extends PersistenceOperation {

  def invoke(uri: UriPath, persistenceConnection: PersistenceConnection) {
    val persistence = persistenceConnection.persistenceFor(entityType)
    val commandContext = persistenceConnection.commandContext
    persistence.find(uri).foreach { readable =>
      val idOpt: Option[ID] = persistence.idOption(readable, uri)
      val writable = persistence.toWritable(persistence.sourceType, readable, uri, persistenceConnection.commandContext)
      persistence.delete(uri)
      val undoDeleteOperation = new PersistenceOperation {
        // make this stateless using a source and sourceType
        def invoke(uri: UriPath, persistenceConnection: PersistenceConnection) {
          persistence.save(idOpt, writable)
        }
      }
      commandContext.allowUndo(Undoable(OperationAction(commandContext.platformDriver.commandToUndoDelete, undoDeleteOperation), None))
    }
  }
}
