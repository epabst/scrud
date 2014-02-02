package com.github.scrud.persistence

import com.github.scrud.{EntityName, EntityType}
import com.github.scrud.state.ApplicationConcurrentMapVal

/**
 * A PersistenceFactory for storing in-memory.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:27 PM
 */
class ListBufferPersistenceFactory[E <: AnyRef](instantiateItem: => E) extends AbstractPersistenceFactory with DataListenerSetValHolder {
  val canSave = true

  private object PersistenceByEntityName extends ApplicationConcurrentMapVal[EntityName,ListBufferCrudPersistence[E]]

  def newWritable() = instantiateItem

  // synchronized so that only one persistence instance is used and onCreateDatabase completes before any thread uses the instance.
  def createEntityPersistence(entityType: EntityType, persistenceConnection: PersistenceConnection) = synchronized {
    val persistenceByEntityName = PersistenceByEntityName.get(persistenceConnection.sharedContext)
    persistenceByEntityName.get(entityType.entityName).getOrElse {
      val persistence = new ListBufferCrudPersistence[E](newWritable(), entityType, persistenceConnection.sharedContext, listenerSet(entityType, persistenceConnection.sharedContext))
      persistenceByEntityName.putIfAbsent(entityType.entityName, persistence)
      entityType.onCreateDatabase(persistence)
      persistence
    }
  }
}
