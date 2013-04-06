package com.github.scrud.persistence

import com.github.scrud.{EntityName, EntityType, CrudContext}
import com.github.scrud.state.ApplicationConcurrentMapVal

/**
 * A PersistenceFactory for storing in-memory.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:27 PM
 */
class ListBufferPersistenceFactory[T <: AnyRef](instantiateItem: => T) extends AbstractPersistenceFactory with DataListenerSetValHolder {
  val canSave = true

  private object PersistenceByEntityName extends ApplicationConcurrentMapVal[EntityName,ListBufferCrudPersistence[T]]

  def newWritable() = instantiateItem

  // synchronized so that only one persistence instance is used and onCreateDatabase completes before any thread uses the instance.
  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = synchronized {
    val persistenceByEntityName = PersistenceByEntityName.get(crudContext.stateHolder)
    persistenceByEntityName.get(entityType.entityName).getOrElse {
      val persistence = new ListBufferCrudPersistence[T](newWritable(), entityType, crudContext, listenerSet(entityType, crudContext))
      persistenceByEntityName.putIfAbsent(entityType.entityName, persistence)
      entityType.onCreateDatabase(persistence)
      persistence
    }
  }
}
