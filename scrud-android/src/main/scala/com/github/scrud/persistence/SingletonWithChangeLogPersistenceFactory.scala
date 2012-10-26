package com.github.scrud.persistence

import com.github.scrud.{UriPath, EntityType}
import com.github.scrud.CrudContext


/**
 * A PersistenceFactory where only the first entity instance is read, and a new instance is saved each time.
 * @author Eric Pabst (epabst@gmail.com)
 */
class SingletonWithChangeLogPersistenceFactory(delegate: PersistenceFactory) extends PersistenceFactory {
  def canSave = delegate.canSave

  override def canDelete = false

  override def canList = false

  def newWritable = delegate.newWritable

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) =
    new SingletonWithChangeLogCrudPersistence(delegate.createEntityPersistence(entityType, crudContext),
      delegate.listenerHolder(entityType, crudContext))

  /** Since the first is used, no ID is required to find one. */
  override def maySpecifyEntityInstance(entityType: EntityType, uri: UriPath) = true

  def listenerHolder(entityType: EntityType, crudContext: CrudContext) = delegate.listenerHolder(entityType, crudContext)
}
