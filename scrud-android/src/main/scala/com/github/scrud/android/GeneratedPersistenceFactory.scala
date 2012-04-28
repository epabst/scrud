package com.github.scrud.android

import common.CachedFunction
import persistence.EntityType

trait GeneratedPersistenceFactory[T <: AnyRef] extends PersistenceFactory with DataListenerSetValHolder {
  def canSave = false

  def newWritable: T = throw new UnsupportedOperationException("not supported")

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext): SeqCrudPersistence[T]
}

object GeneratedPersistenceFactory {
  /** Creates a GeneratedPersistenceFactory given a way to create a SeqCrudPersistence.  The instances are cached by EntityType. */
  def apply[T <: AnyRef](persistenceFunction: EntityType => SeqCrudPersistence[T]): GeneratedPersistenceFactory[T] = new GeneratedPersistenceFactory[T] with DataListenerSetValHolder {
    private val cachedPersistenceFunction = CachedFunction(persistenceFunction)

    def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = cachedPersistenceFunction(entityType)
  }
}
