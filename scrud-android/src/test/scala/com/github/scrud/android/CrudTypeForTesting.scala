package com.github.scrud.android

import action.AndroidOperation._
import org.mockito.Mockito
import com.github.scrud.{EntityName, CrudContext, CrudApplication, EntityType}
import com.github.scrud.persistence._
import com.github.scrud.state.State

/** A simple CrudType for testing.
  * @author Eric Pabst (epabst@gmail.com)
  */
case class CrudTypeForTesting(override val entityType: EntityType, override val persistenceFactory: PersistenceFactory)
  extends CrudType(entityType, persistenceFactory) {

  def this(entityType: EntityType, persistence: CrudPersistence) {
    this(entityType, new PersistenceFactoryForTesting(persistence))
  }

  def this(entityType: EntityType, persistence: ThinPersistence) {
    this(entityType, new CrudPersistenceUsingThin(entityType, persistence))
  }

  def this(entityType: EntityType) {
    this(entityType, Mockito.mock(classOf[ThinPersistence]))
  }

  def this(entityName: EntityName, persistence: ThinPersistence) {
    this(new EntityTypeForTesting(entityName), persistence)
  }

  def this(entityName: EntityName) {
    this(new EntityTypeForTesting(entityName))
  }

  def this(persistence: ThinPersistence) {
    this(new EntityTypeForTesting, persistence)
  }

  def this(persistenceFactory: PersistenceFactory) {
    this(new EntityTypeForTesting, persistenceFactory)
  }

  def this(persistence: CrudPersistence) {
    this(new EntityTypeForTesting, persistence)
  }
}

object CrudTypeForTesting extends CrudTypeForTesting(Mockito.mock(classOf[ThinPersistence]))

class PersistenceFactoryForTesting(persistence: CrudPersistence) extends AbstractPersistenceFactory with DataListenerSetValHolder {
  val canSave = true

  override def newWritable() = Map.empty[String,Any]

  def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = persistence
}

class CrudActivityForTesting(_crudApplication: CrudApplication) extends CrudActivity {
  override lazy val crudApplication = _crudApplication
  override lazy val currentAction = UpdateActionName
  override lazy val applicationState = new State
}
