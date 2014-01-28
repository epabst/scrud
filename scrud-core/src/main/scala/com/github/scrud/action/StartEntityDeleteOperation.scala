package com.github.scrud.action

import com.github.scrud.{CrudContext, UriPath, EntityType}
import com.github.scrud.persistence.CrudPersistence
import com.github.scrud.platform.PlatformTypes._

/**
 * Delete an entity by Uri with an undo option.  It can be wrapped to do a confirmation box if desired.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 11/29/12
 * Time: 7:51 AM
 */
//final to guarantee equality is correct
final case class StartEntityDeleteOperation(entityType: EntityType) extends PersistenceOperation(entityType) {

  def invoke(uri: UriPath, persistence: CrudPersistence, crudContext: CrudContext) {
    persistence.find(uri).foreach { readable =>
      val idOpt: Option[ID] = entityType.findPersistedId(readable)
      val writable = null //todo persistence.toWritable(readable)
      persistence.delete(uri)
      val undoDeleteOperation = new PersistenceOperation(entityType) {
        def invoke(uri: UriPath, persistence: CrudPersistence, crudContext: CrudContext) {
          persistence.save(idOpt, writable)
        }
      }
      crudContext.allowUndo(Undoable(Action(crudContext.platformDriver.commandToUndoDelete, undoDeleteOperation), None))
    }
  }
}
