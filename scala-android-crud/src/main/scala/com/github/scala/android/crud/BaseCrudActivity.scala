package com.github.scala.android.crud

import action.{ActivityWithVars, Action, UriPath}
import com.github.triangle.Logging
import android.view.{MenuItem, Menu}
import android.content.Intent
import common.{Common, Timing, PlatformTypes}

/**
 * Support for the different Crud Activity's.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/25/11
 * Time: 7:01 PM
 */

trait BaseCrudActivity extends ActivityWithVars with PlatformTypes with Logging with Timing {
  def entityType: CrudType

  def application: CrudApplication

  lazy val contentProviderAuthority = Option(application.packageName).getOrElse(getClass.getPackage.getName)
  lazy val defaultContentUri = UriPath("content://" + contentProviderAuthority) / entityType.entityName

  override def setIntent(newIntent: Intent) {
    info("Current Intent: " + newIntent)
    super.setIntent(newIntent)
  }

  def currentUriPath: UriPath = {
    Option(getIntent).map(intent => Option(intent.getData).map(UriPath(_)).getOrElse {
      // If no data was given in the intent (because we were started
      // as a MAIN activity), then use our default content provider.
      intent.setData(Action.toUri(defaultContentUri))
      defaultContentUri
    }).getOrElse(defaultContentUri)
  }

  lazy val currentAction: String = getIntent.getAction

  def uriWithId(id: ID): UriPath = currentUriPath.specify(entityType.entityName, id.toString)

  val crudContext = new CrudContext(this, application)

  protected lazy val logTag = Common.tryToEvaluate(entityType.entityName).getOrElse(Common.logTag)

  protected def applicableActions: List[Action]

  protected def optionsMenuActions: List[Action] =
    applicableActions.filter(action => action.title.isDefined || action.icon.isDefined)

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    val actions = optionsMenuActions
    for (action <- actions) {
      val index = actions.indexOf(action)
      val menuItem = action.title.map(menu.add(0, index, index, _)).getOrElse(menu.add(0, index, index, ""))
      action.icon.map(icon => menuItem.setIcon(icon))
    }
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    val actions = optionsMenuActions
    val action = actions(item.getItemId)
    action.invoke(currentUriPath, this)
    true
  }

  //available to be overridden for testing
  def openEntityPersistence(): CrudPersistence = entityType.openEntityPersistence(crudContext)

  def withPersistence[T](f: CrudPersistence => T): T = {
    val persistence = openEntityPersistence()
    try {
      f(persistence)
    } finally {
      persistence.close()
    }
  }

  def addUndoableDelete(entityType: CrudType, undoable: Undoable[ID]) {
    //todo implement
  }

  override def toString = getClass.getSimpleName + "@" + System.identityHashCode(this)
}
