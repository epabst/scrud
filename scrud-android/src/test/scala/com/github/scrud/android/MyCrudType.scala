package com.github.scrud.android

import action.AndroidOperation._
import org.mockito.Mockito
import com.github.scrud.{CrudContext, CrudApplication, EntityType}
import com.github.scrud.persistence.{PersistenceFactory, CrudPersistence, DataListenerSetValHolder}
import com.github.scrud.platform.{PlatformDriver, TestingPlatformDriver}
import com.github.scrud.EntityName
import com.github.scrud.state.State

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
  val canSave = true

  override def newWritable() = Map.empty[String,Any]

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = persistence
}

class MyCrudApplicationSpecifyingPlatform(platformDriver: PlatformDriver, crudTypes: CrudType*) extends CrudApplication(platformDriver) {
  val name = "test app"

  override lazy val primaryEntityType = crudTypes.head.entityType

  val allCrudTypes = crudTypes.toList

  override def entityNameLayoutPrefixFor(entityName: EntityName) = "test"

  val dataVersion = 1
}

case class MyCrudApplication(crudTypes: CrudType*) extends MyCrudApplicationSpecifyingPlatform(TestingPlatformDriver, crudTypes: _*)

class MyCrudListActivity(_crudApplication: CrudApplication) extends CrudListActivity {
  override lazy val crudApplication = _crudApplication
}

class MyCrudActivity(_crudApplication: CrudApplication) extends CrudActivity {
  override lazy val crudApplication = _crudApplication
  override lazy val currentAction = UpdateActionName
  override lazy val applicationState = new State {}
}
