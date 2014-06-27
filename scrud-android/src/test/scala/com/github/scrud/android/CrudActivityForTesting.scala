package com.github.scrud.android

import action.AndroidOperation._
import com.github.scrud.EntityNavigation
import com.github.scrud.state.State

class CrudActivityForTesting(application: CrudAndroidApplicationLike) extends CrudActivity {

  override final lazy val commandContext: AndroidCommandContext = new AndroidCommandContextForTesting(application, this)

  override def entityNavigation: EntityNavigation = super.entityNavigation
  override lazy val entityType = entityNavigation.primaryEntityType

  override lazy val currentAction = UpdateActionName
  override lazy val applicationState = new State

  //make it public for testing
  override def onPause() {
    super.onPause()
  }

  //make it public for testing
  override def onResume() {
    super.onResume()
  }
}
