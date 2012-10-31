package com.github.scrud.android

import org.mockito.Mockito
import com.github.scrud.{CrudContext, EntityName, CrudApplication, EntityType}
import com.github.scrud.persistence.{PersistenceFactory, CrudPersistence, DataListenerSetValHolder}
import com.github.scrud.platform.TestingPlatformDriver

/** A simple CrudType for testing.
  * @author Eric Pabst (epabst@gmail.com)
  */
case class MyCrudType(override val entityType: EntityType, override val persistenceFactory: PersistenceFactory)
  extends CrudType(entityType, persistenceFactory) {

  def this(entityType: EntityType, persistence: CrudPersistence = Mockito.mock(classOf[CrudPersistence])) {
    this(entityType, new MyPersistenceFactory(persistence))
  }

  def this(persistenceFactory: PersistenceFactory) {
    this(new MyEntityType, persistenceFactory)
  }

  def this(persistence: CrudPersistence) {
    this(new MyEntityType, persistence)
  }
}

object MyCrudType extends MyCrudType(Mockito.mock(classOf[CrudPersistence]))

class MyPersistenceFactory(persistence: CrudPersistence) extends PersistenceFactory with DataListenerSetValHolder {
  def canSave = true

  override def newWritable = Map.empty[String,Any]

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = persistence
}

case class MyCrudApplication(crudTypes: CrudType*) extends CrudApplication(TestingPlatformDriver) {
  def name = "test app"

  override def primaryEntityType = crudTypes.head.entityType

  def allCrudTypes = crudTypes.toList

  override def entityNameLayoutPrefixFor(entityName: EntityName) = "test"

  def dataVersion = 1
}