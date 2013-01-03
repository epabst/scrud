package com.github.scrud.persistence

import com.github.scrud.{EntityName, UriPath, EntityType, CrudContext}

/** A PersistenceFactory that is derived from related CrudType persistence(s).
  * @author Eric Pabst (epabst@gmail.com)
  */
abstract class DerivedPersistenceFactory[T <: AnyRef](delegates: EntityName*) extends GeneratedPersistenceFactory[T] { self =>
  def findAll(entityType: EntityType, uri: UriPath, crudContext: CrudContext, delegatePersistenceMap: Map[EntityName,CrudPersistence]): Seq[T]

  def createEntityPersistence(_entityType: EntityType, crudContext: CrudContext) = {
    new DerivedCrudPersistence[T](crudContext, listenerSet(_entityType, crudContext), delegates: _*) {
      def entityType = _entityType

      def findAll(uri: UriPath): Seq[T] = self.findAll(_entityType, uri, crudContext, delegatePersistenceMap)
    }
  }
}
