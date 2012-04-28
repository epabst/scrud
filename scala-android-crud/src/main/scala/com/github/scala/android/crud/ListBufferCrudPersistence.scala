package com.github.scala.android.crud

import common.{CachedFunction, MutableListenerSet, ListenerSet}
import persistence.{ListBufferEntityPersistence, DataListener, EntityType, SeqEntityPersistence}
import collection.mutable

trait SeqCrudPersistence[T <: AnyRef] extends SeqEntityPersistence[T] with CrudPersistence

class ListBufferCrudPersistence[T <: AnyRef](newWritableFunction: => T, val entityType: EntityType,
                                             val crudContext: CrudContext,
                                             listenerSet: ListenerSet[DataListener] = new MutableListenerSet[DataListener])
        extends ListBufferEntityPersistence[T](newWritableFunction, listenerSet) with SeqCrudPersistence[T]

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
