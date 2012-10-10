package com.github.scrud.android

import action._
import action.Action
import action.CrudOperation
import android.view.{View, MenuItem}
import android.content.Intent
import common.{Timing, UriPath, Common, PlatformTypes}
import persistence.EntityType
import PlatformTypes._
import com.github.scrud.android.view.AndroidConversions._
import android.os.Bundle
import com.github.triangle._
import view.{ViewField, OnClickOperationSetter}
import android.app.Activity
import scala.Some

/** Support for the different Crud Activity's.
  * @author Eric Pabst (epabst@gmail.com)
  */

trait BaseCrudActivity extends ActivityWithState with OptionsMenuActivity with Logging with Timing { self =>
  lazy val platformDriver = new AndroidPlatformDriver(this, logTag)

  def runOnUiThread[T](view: View)(body: => T) {
    platformDriver.runOnUiThread(view)(trackWorkInProgress(body).apply())
  }

  def runOnUiThread[T](activity: Activity)(body: => T) {
    platformDriver.runOnUiThread(activity)(trackWorkInProgress(body).apply())
  }

  def crudApplication: CrudApplication = super.getApplication.asInstanceOf[CrudAndroidApplication].application

  lazy val crudType: CrudType = crudApplication.allCrudTypes.find(crudType => Some(crudType.entityName) == currentUriPath.lastEntityNameOption).getOrElse {
    throw new IllegalStateException("No valid entityName in " + currentUriPath)
  }

  final def entityType = crudType.entityType

  override def setIntent(newIntent: Intent) {
    info("Current Intent: " + newIntent)
    super.setIntent(newIntent)
  }

  def currentUriPath: UriPath = {
    val defaultContentUri = crudApplication.defaultContentUri
    Option(getIntent).map(intent => Option(intent.getData).map(toUriPath(_)).getOrElse {
      // If no data was given in the intent (because we were started
      // as a MAIN activity), then use our default content provider.
      intent.setData(defaultContentUri)
      defaultContentUri
    }).getOrElse(defaultContentUri)
  }

  def currentCrudOperation: CrudOperation = CrudOperation(entityType, currentCrudOperationType)

  private def currentCrudOperationType: CrudOperationType.Value = currentAction match {
    case Operation.CreateActionName => CrudOperationType.Create
    case Operation.ListActionName => CrudOperationType.List
    // This would normally be Operation.ListActionName, but it is the starting intent.
    case Intent.ACTION_MAIN => CrudOperationType.List
    case Operation.DisplayActionName => CrudOperationType.Read
    case Operation.UpdateActionName => CrudOperationType.Update
    case Operation.DeleteActionName => CrudOperationType.Delete
  }

  lazy val currentAction: String = getIntent.getAction

  def uriWithId(id: ID): UriPath = currentUriPath.specify(entityType.entityName, id.toString)

  lazy val crudContext = new CrudContext(this, crudApplication)

  def contextItems = GetterInput(currentUriPath, crudContext, PortableField.UseDefaults)

  def contextItemsWithoutUseDefaults = GetterInput(currentUriPath, crudContext)

  protected lazy val logTag = Common.tryToEvaluate(crudApplication.name).getOrElse(Common.logTag)

  /** This should be a lazy val in subclasses. */
  protected def normalActions: Seq[Action]

  def populateFromUri(entityType: EntityType, uri: UriPath) {
    val futurePortableValue = crudApplication.futurePortableValue(entityType, uri, crudContext)
    val updaterInput = UpdaterInput(this, contextItems)
    if (futurePortableValue.isSet) {
      futurePortableValue().update(updaterInput)
    } else {
      entityType.defaultValue.update(updaterInput)
      futurePortableValue.foreach { portableValue =>
        platformDriver.runOnUiThread(self) {
          portableValue.update(updaterInput)
        }
      }
    }
  }

  /** A StateVar that holds an undoable Action if present. */
  private object LastUndoable extends StateVar[Undoable]

  def allowUndo(undoable: Undoable) {
    // Finish any prior undoable first.  This could be re-implemented to support a stack of undoable operations.
    LastUndoable.clear(this).foreach(_.closeOperation.foreach(_.invoke(currentUriPath, this)))
    // Remember the new undoable operation
    LastUndoable.set(this, undoable)
    optionsMenuCommands = generateOptionsMenu.map(_.command)
  }

  def pickLayout(entityType: EntityType): LayoutKey = crudApplication.crudType(entityType).pickLayout

  protected def applicableActions: List[Action] = LastUndoable.get(this).map(_.undoAction).toList ++ normalActions

  protected lazy val normalOperationSetters: FieldList = {
    val setters = normalActions.filter(_.command.viewRef.isDefined).map(action =>
      ViewField.viewId[Nothing](action.command.viewRef.get, OnClickOperationSetter(_ => action.operation)))
    FieldList.toFieldList(setters)
  }

  protected def bindNormalActionsToViews() {
    normalOperationSetters.defaultValue.update(this, contextItems)
  }

  protected def generateOptionsMenu: List[Action] =
    applicableActions.filter(action => action.command.title.isDefined || action.command.icon.isDefined)

  def initialOptionsMenuCommands = generateOptionsMenu.map(_.command)

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    withExceptionReportingHavingDefaultReturnValue(exceptionalReturnValue = true) {
      val actions = generateOptionsMenu
      actions.find(_.commandId == item.getItemId) match {
        case Some(action) =>
          action.invoke(currentUriPath, this)
          if (LastUndoable.get(this).exists(_.undoAction.commandId == item.getItemId)) {
            LastUndoable.clear(this)
            optionsMenuCommands = generateOptionsMenu.map(_.command)
          }
          true
        case None => super.onOptionsItemSelected(item)
      }
    }
  }

  override def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    withExceptionReporting {
      // This is after the super call so that outState can be overridden if needed.
      crudContext.onSaveActivityState(outState)
    }
  }

  override def onRestoreInstanceState(savedInstanceState: Bundle) {
    withExceptionReporting {
      // This is before the super call to be the opposite order as onSaveInstanceState.
      crudContext.onRestoreActivityState(savedInstanceState)
    }
    super.onRestoreInstanceState(savedInstanceState)
  }

  override def onResume() {
    withExceptionReporting {
      trace("onResume")
      crudContext.onClearActivityState(stayActive = true)
    }
    super.onResume()
  }

  override def onDestroy() {
    withExceptionReporting {
      crudContext.activityState.onDestroyState()
    }
    super.onDestroy()
  }

  override def waitForWorkInProgress() {
    val start = System.currentTimeMillis()
    super.waitForWorkInProgress()
    debug("Waited for work in progress for " + (System.currentTimeMillis() - start) + "ms")
  }

  override def toString = getClass.getSimpleName + "@" + System.identityHashCode(this)
}
