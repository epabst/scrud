package com.github.scrud.persistence

import com.github.scrud.{CrudContext, EntityType}

/**
 * A PersistenceFactory to use during tests.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/28/14
 *         Time: 9:47 AM
 */
class PersistenceFactoryForTesting(persistence: CrudPersistence) extends AbstractPersistenceFactory with DataListenerSetValHolder {
  def this(entityType: EntityType, thinPersistence: ThinPersistence) = 
    this(new CrudPersistenceUsingThin(entityType, thinPersistence))
  
  val canSave = true

  override def newWritable() = Map.empty[String,Any]

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = persistence

  def toEntityTypeMap: EntityTypeMap = EntityTypeMap(persistence.entityType -> this)
}
