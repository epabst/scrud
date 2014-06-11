package com.github.scrud.android

import action.AndroidOperation._
import org.mockito.Mockito
import com.github.scrud.{EntityNavigation, EntityName, CrudApplication, EntityType}
import com.github.scrud.persistence._
import com.github.scrud.state.State

class CrudActivityForTesting extends CrudActivity {

  override lazy val commandContext: AndroidCommandContext = _

  override def entityNavigation: EntityNavigation = super.entityNavigation

  override lazy val currentAction = UpdateActionName
  override lazy val applicationState = new State
}
