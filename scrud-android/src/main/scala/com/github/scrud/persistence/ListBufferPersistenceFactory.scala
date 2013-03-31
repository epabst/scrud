package com.github.scrud.persistence

import com.github.scrud.{EntityName, EntityType, CrudContext}
import collection.mutable
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import com.github.scrud.state.LazyApplicationVal

/**
 * A PersistenceFactory for storing in-memory.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:27 PM
 */
class ListBufferPersistenceFactory[T <: AnyRef](instantiateItem: => T) extends PersistenceFactory with DataListenerSetValHolder {
  val canSave = true

  private object PersistenceByEntityName extends LazyApplicationVal[mutable.ConcurrentMap[EntityName,ListBufferCrudPersistence[T]]](
    new ConcurrentHashMap[EntityName,ListBufferCrudPersistence[T]]())

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
