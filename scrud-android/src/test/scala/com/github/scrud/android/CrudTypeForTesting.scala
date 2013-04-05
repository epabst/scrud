package com.github.scrud.android

import action.AndroidOperation._
import org.mockito.Mockito
import com.github.scrud.{CrudContext, CrudApplication, EntityType}
import com.github.scrud.persistence.{AbstractPersistenceFactory, PersistenceFactory, CrudPersistence, DataListenerSetValHolder}
import com.github.scrud.state.State

/** A simple CrudType for testing.
  * @author Eric Pabst (epabst@gmail.com)
  */
case class CrudTypeForTesting(override val entityType: EntityType, override val persistenceFactory: PersistenceFactory)
  extends CrudType(entityType, persistenceFactory) {

  def this(entityType: EntityType, persistence: CrudPersistence = Mockito.mock(classOf[CrudPersistence])) {
    this(entityType, new PersistenceFactoryForTesting(persistence))
  }

  def this(persistenceFactory: PersistenceFactory) {
    this(new EntityTypeForTesting, persistenceFactory)
  }

  def this(persistence: CrudPersistence) {
    this(new EntityTypeForTesting, persistence)
  }
}

object CrudTypeForTesting extends CrudTypeForTesting(Mockito.mock(classOf[CrudPersistence]))

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
