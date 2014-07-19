package com.github.scrud.android

import _root_.android.app.Activity
import _root_.android.view.View
import _root_.android.widget.{AdapterView, Adapter}
import action.AndroidNotification
import com.github.scrud.state._
import com.github.scrud._
import com.github.scrud.android.persistence.{CrudContentProvider, ContentResolverEntityTypeMap}
import state.{ActivityStateHolder, ActivityVar, CachedStateListeners, CachedStateListener}
import _root_.android.os.{Looper, Bundle}
import _root_.android.content.Context
import _root_.android.telephony.TelephonyManager
import com.github.scrud.android.backup.CrudBackupAgent
import com.github.scrud.context.{SimpleSharedContext, SharedContext, CommandContext}
import com.github.annotations.quality.MicrotestCompatible
import com.github.scrud.persistence.{PersistenceConnection, EntityTypeMap}
import com.github.scrud.copy.{SourceType, AdaptedValueSeq, TargetType}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.action.Undoable

/**
 * The context and state for the application code to interact with.
 * A context which can store data for the duration of a single Activity.
 * @author Eric Pabst (epabst@gmail.com)
 */
@MicrotestCompatible(use = "new AndroidCommandContextForTesting(...)")
case class AndroidCommandContext(context: Context, stateHolder: ActivityStateHolder, override val entityTypeMap: EntityTypeMap, androidApplication: CrudAndroidApplicationLike)
    extends CommandContext with AndroidNotification {
  def this(context: Context, stateHolder: ActivityStateHolder, androidApplication: CrudAndroidApplicationLike) {
    this(context, stateHolder, stateHolder match {
      case _: CrudContentProvider =>
        androidApplication.entityTypeMap
      case _: CrudBackupAgent =>
        androidApplication.entityTypeMap
      case _ =>
        context match {
          case _: Activity =>
            // Use a ContentResolver (this should never be called from the ContentProvider).
            new ContentResolverEntityTypeMap(androidApplication.entityTypeMap)
          case _ =>
            androidApplication.entityTypeMap
        }
    }, androidApplication)
  }

  def this(activityContext: Context with ActivityStateHolder, androidApplication: CrudAndroidApplicationLike) {
    this(activityContext, activityContext, androidApplication)
  }
  
  override def entityNavigation: EntityNavigation = androidApplication.entityNavigation

  /** Fails if the current Context is not an Activity. */
  def activity: Activity = context.asInstanceOf[Activity]

  //final since only here as a convenience method.
  final def activityState: State = stateHolder.activityState

  lazy val androidPlatformDriver = platformDriver.asInstanceOf[AndroidPlatformDriver]

  lazy val dataVersion: Int = androidPlatformDriver.calculateDataVersion(androidApplication.entityTypeMap.allEntityTypes)

  override final def sharedContext: SharedContext = androidApplication

  def populateFromUri(entityType: EntityType, uri: UriPath, targetType: TargetType, uiTarget: AnyRef) {
    val futurePortableValueOpt = androidApplication.futurePortableValueOpt(entityType, uri, targetType, this)
    populateFromFutureValueSeq(futurePortableValueOpt, entityType, uri, targetType, uiTarget)
  }

  def populateFromSource(entityType: EntityType, sourceType: SourceType, source: AnyRef, sourceUri: UriPath, targetType: TargetType, targetView: View) {
    targetView.setTag(sourceUri)
    val futurePortableValueOpt = androidApplication.futurePortableValueOpt(entityType, sourceType, source, sourceUri, targetType, this)
    populateFromFutureValueSeq(futurePortableValueOpt, entityType, sourceUri, targetType, targetView)
  }

  def populateFromFutureValueSeq(futurePortableValueOpt: Future[Option[AdaptedValueSeq]], entityType: EntityType, sourceUri: UriPath, targetType: TargetType, uiTarget: AnyRef) {
    if (!futurePortableValueOpt.isCompleted) {
      entityType.copyAndUpdate(LoadingIndicator, LoadingIndicator, sourceUri, targetType, uiTarget, this)
    } else {
      debug("Copying into " + uiTarget + " immediately")
    }
    futurePortableValueOpt.onSuccess {
      case adaptedValueSeqOpt =>
        val adaptedValueSeqOrEmpty = adaptedValueSeqOpt.getOrElse(commandContext.findDefault(entityType, sourceUri, targetType))
        populateFromValueSeq(adaptedValueSeqOrEmpty, uiTarget)
    }
  }

  /**
   * Update the uiTarget with the values in adaptedValueSeq.
   * @param adaptedValueSeq the values to use
   * @param uiTarget an Activity or a View to update
   */
  def populateFromValueSeq(adaptedValueSeq: AdaptedValueSeq, uiTarget: AnyRef) {
    runOnUiThread {
      val viewOpt = uiTarget match {
        case view: View => Some(view)
        case _ => None
      }
      if (viewOpt.forall(_.getTag == adaptedValueSeq.sourceUri)) {
        debug("Copying " + adaptedValueSeq + " into " + uiTarget + " uri=" + adaptedValueSeq.sourceUri)
        adaptedValueSeq.update(uiTarget)
        viewOpt.foreach(_.invalidate())
      }
    }
  }

  /** The ISO 2 country such as "US". */
  lazy val isoCountry = {
    Option(context.getSystemService(Context.TELEPHONY_SERVICE)).map(_.asInstanceOf[TelephonyManager]).
        flatMap(tm => Option(tm.getSimCountryIso)).getOrElse(java.util.Locale.getDefault.getCountry)
  }

  /** Provides a way for the user to undo an operation. */
  override def allowUndo(undoable: Undoable) {
    // Finish any prior undoable first.  This could be re-implemented to support a stack of undoable operations.
    LastUndoable.clear(stateHolder).foreach(_.closeOperation.foreach(_.invoke(UriPath.EMPTY, this)))
    // Remember the new undoable operation
    LastUndoable.set(stateHolder, undoable)

    context match {
      case crudActivity: CrudActivity =>
        crudActivity.onCommandsChanged()
    }
  }

  private[android] def isUIThread: Boolean = Looper.myLooper() == Looper.getMainLooper

  override def persistenceConnection: PersistenceConnection = {
    if (PersistenceDeniedInUIThread.get(stateHolder).getOrElse(false)) {
      if (isUIThread) {
        throw new IllegalStateException("Do this on another thread!")
      }
    }
    persistenceConnectionVal
  }

  /**
   * Handle the exception by communicating it to the user and developers.
   */
  override def reportError(throwable: Throwable) {
    LastException.set(stateHolder, throwable)
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

  def setListAdapter[A <: Adapter](adapterView: AdapterView[A], entityType: EntityType, uri: UriPath, targetType: TargetType, itemLayout: LayoutKey) {
    activity.asInstanceOf[CrudActivity].setListAdapter(adapterView, entityType, uri, targetType, activity, itemLayout)
  }
}

/** A StateVar that holds an undoable Action if present. */
private object LastUndoable extends ActivityVar[Undoable]

private[android] object LastException extends ApplicationVar[Throwable]

private[android] object PersistenceDeniedInUIThread extends ApplicationVar[Boolean]
