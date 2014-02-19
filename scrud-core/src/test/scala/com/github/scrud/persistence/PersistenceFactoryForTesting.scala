package com.github.scrud.persistence

import com.github.scrud.EntityType
import com.github.scrud.copy.types.MapStorage
import org.scalatest.mock.MockitoSugar

/**
 * A PersistenceFactory to use during tests.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/28/14
 *         Time: 9:47 AM
 */
class PersistenceFactoryForTesting(entityType: EntityType, val thinPersistence: ThinPersistence = MockitoSugar.mock[ThinPersistence])
    extends AbstractPersistenceFactory with DataListenerSetValHolder {
  val canSave = true

  override def newWritable() = new MapStorage

  def createEntityPersistence(entityType: EntityType, persistenceConnection: PersistenceConnection) =
    new CrudPersistenceUsingThin(entityType, thinPersistence, persistenceConnection.sharedContext)

  def toTuple: (EntityType,PersistenceFactory) = entityType -> this
}
