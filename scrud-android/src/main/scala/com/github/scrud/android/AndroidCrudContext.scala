package com.github.scrud.android

import _root_.android.app.Activity
import action.AndroidNotification
import com.github.scrud.state._
import com.github.scrud._
import state.{ActivityStateHolder, ActivityVar, CachedStateListeners, CachedStateListener}
import _root_.android.os.{Looper, Bundle}
import _root_.android.content.Context
import _root_.android.telephony.TelephonyManager
import com.github.scrud.action.Undoable

/**
 * The context and state for the application code to interact with.
 * A context which can store data for the duration of a single Activity.
 * @author Eric Pabst (epabst@gmail.com)
 */
case class AndroidCrudContext(context: Context, stateHolder: ActivityStateHolder, application: CrudApplication) extends CrudContext with AndroidNotification {
  def this(activityContext: Context with ActivityStateHolder, application: CrudApplication) {
    this(activityContext, activityContext, application)
  }

  /** Fails if the current Context is not an Activity. */
  def activity: Activity = context.asInstanceOf[Activity]

  /** Fails if the current Context is not an ActivityWithState. */
  def activityState: State = stateHolder.activityState

  lazy override val platformDriver: AndroidPlatformDriver = application.platformDriver.asInstanceOf[AndroidPlatformDriver]
  lazy val applicationState: State = stateHolder.applicationState

  /** The ISO 2 country such as "US". */
  lazy val isoCountry = {
    Option(context.getSystemService(Context.TELEPHONY_SERVICE)).map(_.asInstanceOf[TelephonyManager]).
        flatMap(tm => Option(tm.getSimCountryIso)).getOrElse(java.util.Locale.getDefault.getCountry)
  }

  /** Provides a way for the user to undo an operation. */
  def allowUndo(undoable: Undoable) {
    // Finish any prior undoable first.  This could be re-implemented to support a stack of undoable operations.
    LastUndoable.clear(this).foreach(_.closeOperation.foreach(_.invoke(UriPath.EMPTY, this)))
    // Remember the new undoable operation
    LastUndoable.set(this, undoable)

    context match {
      case crudActivity: CrudActivity =>
        crudActivity.onCommandsChanged()
    }
  }

  private[android] def isUIThread: Boolean = Looper.myLooper() == Looper.getMainLooper

  override def openEntityPersistence(entityType: EntityType) = {
    if (PersistenceDeniedInUIThread.get(this).getOrElse(false)) {
      if (isUIThread) {
        throw new IllegalStateException("Do this on another thread!")
      }
    }
    super.openEntityPersistence(entityType)
  }

  /**
   * Handle the exception by communicating it to the user and developers.
   */
  override def reportError(throwable: Throwable) {
    LastException.set(this, throwable)
    super.reportError(throwable)
  }

  def addCachedActivityStateListener(listener: CachedStateListener) {
    CachedStateListeners.get(this) += listener
  }

  def onSaveActivityState(outState: Bundle) {
    CachedStateListeners.get(this).foreach(_.onSaveState(outState))
  }

  def onRestoreActivityState(savedState: Bundle) {
    CachedStateListeners.get(this).foreach(_.onRestoreState(savedState))
  }

  def onClearActivityState(stayActive: Boolean) {
    CachedStateListeners.get(this).foreach(_.onClearState(stayActive))
  }
}

/** A StateVar that holds an undoable Action if present. */
private object LastUndoable extends ActivityVar[Undoable]

private[android] object LastException extends ApplicationVar[Throwable]

private[android] object PersistenceDeniedInUIThread extends ApplicationVar[Boolean]
