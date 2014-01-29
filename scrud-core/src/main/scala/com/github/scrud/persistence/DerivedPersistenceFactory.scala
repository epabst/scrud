package com.github.scrud.persistence

import com.github.scrud.{EntityName, UriPath, EntityType}

/** A PersistenceFactory that is derived from related CrudType persistence(s).
  * @author Eric Pabst (epabst@gmail.com)
  */
abstract class DerivedPersistenceFactory[T <: AnyRef](delegates: EntityName*) extends GeneratedPersistenceFactory[T] { self =>
  def findAll(entityType: EntityType, uri: UriPath, persistenceConnection: PersistenceConnection): Seq[T]

  def createEntityPersistence(_entityType: EntityType, persistenceConnection: PersistenceConnection) = {
    new DerivedCrudPersistence[T](persistenceConnection, listenerSet(_entityType, persistenceConnection.sharedContext), delegates: _*) {
      val entityType = _entityType

      def findAll(uri: UriPath): Seq[T] = self.findAll(_entityType, uri, persistenceConnection)
    }
  }
}
