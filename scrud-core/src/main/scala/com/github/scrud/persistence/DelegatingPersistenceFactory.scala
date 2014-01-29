package com.github.scrud.persistence

import com.github.scrud.{UriPath, EntityName, EntityType}

/**
 * A [[com.github.scrud.persistence.PersistenceFactory]] that delegates all its calls to another.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/4/13
 * Time: 7:25 AM
 */
abstract class DelegatingPersistenceFactory(delegate: PersistenceFactory) extends PersistenceFactory {
  def canSave = delegate.canSave

  def canCreate = delegate.canCreate

  def canDelete = delegate.canDelete

  def canList = delegate.canList

  def maySpecifyEntityInstance(entityName: EntityName, uri: UriPath) = delegate.maySpecifyEntityInstance(entityName, uri)

  def newWritable() = delegate.newWritable()

  def createEntityPersistence(entityType: EntityType, persistenceConnection: PersistenceConnection) =
    delegate.createEntityPersistence(entityType, persistenceConnection)
}
