package com.github.scrud.persistence

import com.github.scrud.EntityType
import collection.mutable
import com.github.scrud.CrudContext
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._

/**
 * A PersistenceFactory for storing in-memory.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:27 PM
 */
class ListBufferPersistenceFactory[T <: AnyRef](instantiateItem: => T) extends PersistenceFactory with DataListenerSetValHolder {
  val canSave = true

  private val persistenceByEntityType: mutable.ConcurrentMap[EntityType,ListBufferCrudPersistence[T]] = new ConcurrentHashMap[EntityType,ListBufferCrudPersistence[T]]()

  def newWritable() = instantiateItem

  // synchronized so that only one persistence instance is used and onCreateDatabase completes before any thread uses the instance.
  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = synchronized {
    persistenceByEntityType.get(entityType).getOrElse {
      val persistence = new ListBufferCrudPersistence[T](newWritable, entityType, crudContext, listenerSet(entityType, crudContext))
      entityType.onCreateDatabase(persistence)
      persistenceByEntityType.putIfAbsent(entityType, persistence)
      persistence
    }
  }
}
