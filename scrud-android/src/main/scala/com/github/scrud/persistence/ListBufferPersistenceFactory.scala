package com.github.scrud.persistence

import com.github.scrud.util.CachedFunction
import com.github.scrud.EntityType
import collection.mutable
import com.github.scrud.android.CrudContext

/**
 * A PersistenceFactory for storing in-memory.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:27 PM
 */
class ListBufferPersistenceFactory[T <: AnyRef](instantiateItem: => T) extends PersistenceFactory with DataListenerSetValHolder {
  def canSave = true

  private val cachedListBuffers = new CachedFunction[EntityType,mutable.ListBuffer[T]](entityType => mutable.ListBuffer[T]())
  private def listBuffer(entityType: EntityType): mutable.ListBuffer[T] = cachedListBuffers(entityType)

  def newWritable = instantiateItem

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) =
    new ListBufferCrudPersistence[T](newWritable, entityType, crudContext, listenerSet(entityType, crudContext)) {
      //make the listBuffer last more than one openEntityPersistence
      override val buffer = listBuffer(entityType)
    }
}
