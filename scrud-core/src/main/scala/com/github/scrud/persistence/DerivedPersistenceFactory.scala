package com.github.scrud.persistence

import com.github.scrud.{EntityName, UriPath, EntityType}
import com.github.scrud.context.SharedContext

/** A PersistenceFactory that is derived from related CrudType persistence(s).
  * @author Eric Pabst (epabst@gmail.com)
  */
abstract class DerivedPersistenceFactory[T <: AnyRef](delegates: EntityName*) extends GeneratedPersistenceFactory[T] { self =>
  def findAll(entityType: EntityType, uri: UriPath, sharedContext: SharedContext, delegatePersistenceMap: Map[EntityName,CrudPersistence]): Seq[T]

  def createEntityPersistence(_entityType: EntityType, sharedContext: SharedContext) = {
    new DerivedCrudPersistence[T](sharedContext, listenerSet(_entityType, sharedContext), delegates: _*) {
      def entityType = _entityType

      def findAll(uri: UriPath): Seq[T] = self.findAll(_entityType, uri, sharedContext, delegatePersistenceMap)
    }
  }
}
