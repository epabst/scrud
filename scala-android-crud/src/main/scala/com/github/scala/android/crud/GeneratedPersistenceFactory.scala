package com.github.scala.android.crud

import common.{ListenerHolder, NoopListenerHolder, CachedFunction}
import persistence.{PersistenceListener, EntityType}

trait GeneratedPersistenceFactory[T <: AnyRef] extends PersistenceFactory {
  def canSave = false

  def newWritable: T = throw new UnsupportedOperationException("not supported")

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext): SeqCrudPersistence[T]

  def listenerHolder(entityType: EntityType, crudContext: CrudContext): ListenerHolder[PersistenceListener] =
    NoopPersistenceListenerHolder
}

private object NoopPersistenceListenerHolder extends NoopListenerHolder[PersistenceListener]

object GeneratedPersistenceFactory {
  /** Creates a GeneratedPersistenceFactory given a way to create a SeqCrudPersistence.  The instances are cached by EntityType. */
  def apply[T <: AnyRef](persistenceFunction: EntityType => SeqCrudPersistence[T]): GeneratedPersistenceFactory[T] = new GeneratedPersistenceFactory[T] {
    private val cachedPersistenceFunction = CachedFunction(persistenceFunction)

    def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = cachedPersistenceFunction(entityType)
  }
}
