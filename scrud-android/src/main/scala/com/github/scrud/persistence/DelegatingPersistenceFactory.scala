package com.github.scrud.persistence

import com.github.scrud.{UriPath, EntityName, CrudContext, EntityType}

/**
 * A [[com.github.scrud.persistence.PersistenceFactory]] that delegates all its calls to another.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/4/13
 * Time: 7:25 AM
 */
class DelegatingPersistenceFactory(delegate: PersistenceFactory) extends PersistenceFactory {
  def canSave = delegate.canSave

  def canCreate = delegate.canCreate

  def canDelete = delegate.canDelete

  def canList = delegate.canList

  def maySpecifyEntityInstance(entityName: EntityName, uri: UriPath) = delegate.maySpecifyEntityInstance(entityName, uri)

  def newWritable() = delegate.newWritable()

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) =
    delegate.createEntityPersistence(entityType, crudContext)

  def listenerHolder(entityType: EntityType, crudContext: CrudContext) = delegate.listenerHolder(entityType, crudContext)
}
