package com.github.scrud.android

import action.AndroidOperation._
import org.mockito.Mockito
import com.github.scrud.{EntityName, CrudApplication, EntityType}
import com.github.scrud.persistence._
import com.github.scrud.state.State



object CrudTypeForTesting extends CrudTypeForTesting(Mockito.mock(classOf[ThinPersistence]))

class CrudActivityForTesting(_crudApplication: CrudApplication) extends CrudActivity {
  override lazy val crudApplication = _crudApplication
  override lazy val currentAction = UpdateActionName
  override lazy val applicationState = new State
}
