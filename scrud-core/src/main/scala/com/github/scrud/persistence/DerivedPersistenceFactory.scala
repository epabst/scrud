package com.github.scrud.persistence

import com.github.scrud.{EntityName, UriPath, EntityType}

/** A PersistenceFactory that is derived from related CrudType persistence(s).
  * @author Eric Pabst (epabst@gmail.com)
  */
abstract class DerivedPersistenceFactory[E <: AnyRef](delegates: EntityName*) extends GeneratedPersistenceFactory[E] { self =>
  def findAll(entityType: EntityType, uri: UriPath, persistenceConnection: PersistenceConnection): Seq[E]

  def createEntityPersistence(_entityType: EntityType, persistenceConnection: PersistenceConnection) = {
    new DerivedCrudPersistence[E](persistenceConnection, listenerSet(_entityType, persistenceConnection.sharedContext), delegates: _*) {
      val entityType = _entityType

      def findAll(uri: UriPath): Seq[E] = self.findAll(_entityType, uri, persistenceConnection)
    }
  }
}
