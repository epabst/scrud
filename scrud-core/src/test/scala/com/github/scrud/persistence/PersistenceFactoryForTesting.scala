package com.github.scrud.persistence

import com.github.scrud.copy.types.MapStorage
import com.github.scrud.EntityType

/**
 * A PersistenceFactory to use during tests.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/28/14
 *         Time: 9:47 AM
 */
class PersistenceFactoryForTesting(thinPersistenceOpt: Option[ThinPersistence] = None) extends ListBufferPersistenceFactory[MapStorage](new MapStorage) {
  def this(thinPersistence: ThinPersistence) {
    this(Some(thinPersistence))
  }

  override def createEntityPersistence(entityType: EntityType, persistenceConnection: PersistenceConnection): CrudPersistence = {
    thinPersistenceOpt.fold(super.createEntityPersistence(entityType, persistenceConnection))(
      new CrudPersistenceUsingThin(entityType, _, persistenceConnection.sharedContext, listenerHolder(entityType, persistenceConnection.sharedContext)))
  }
}

object PersistenceFactoryForTesting extends PersistenceFactoryForTesting(None)
