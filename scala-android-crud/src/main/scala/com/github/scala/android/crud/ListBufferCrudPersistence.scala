package com.github.scala.android.crud

import common.{CachedFunction, MutableListenerSet, ListenerSet}
import persistence.{ListBufferEntityPersistence, PersistenceListener, EntityType, SeqEntityPersistence}
import collection.mutable

trait SeqCrudPersistence[T <: AnyRef] extends SeqEntityPersistence[T] with CrudPersistence

class ListBufferCrudPersistence[T <: AnyRef](newWritableFunction: => T, val entityType: EntityType,
                                             val crudContext: CrudContext,
                                             listenerSet: ListenerSet[PersistenceListener] = new MutableListenerSet[PersistenceListener])
        extends ListBufferEntityPersistence[T](newWritableFunction, listenerSet) with SeqCrudPersistence[T]

class ListBufferPersistenceFactory[T <: AnyRef](instantiateItem: => T) extends PersistenceFactory with PersistenceListenerSetValHolder {
  def canSave = true

  private val cachedListBuffers = new CachedFunction[EntityType,mutable.ListBuffer[T]](entityType => mutable.ListBuffer[T]())
  private def listBuffer(entityType: EntityType): mutable.ListBuffer[T] = cachedListBuffers(entityType)

  def newWritable = instantiateItem

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) =
    new ListBufferCrudPersistence[T](newWritable, entityType, crudContext, listenerSet(entityType, crudContext)) {
      //make the listBuffer last more than one openEntityPersistence
      override val buffer = listBuffer(entityType)
    }

  def addListener(listener: PersistenceListener, entityType: EntityType, crudContext: CrudContext) {
    listenerSet(entityType, crudContext).addListener(listener)
  }
}
