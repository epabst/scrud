package com.github.scrud.android

import action.{AndroidNotification, ContextWithState}
import com.github.scrud.state._
import com.github.scrud.{CrudContext, CrudApplication}
import state.{CachedStateListeners, CachedStateListener}
import android.os.Bundle
import android.content.Context
import android.telephony.TelephonyManager

/**
 * The context and state for the application code to interact with.
 * A context which can store data for the duration of a single Activity.
 * @author Eric Pabst (epabst@gmail.com)
 */
case class AndroidCrudContext(activityContext: ContextWithState, application: CrudApplication) extends CrudContext with AndroidNotification {
  lazy val platformDriver = AndroidPlatformDriver
  def activityState: State = activityContext
  lazy val applicationState: State = activityContext.applicationState

  /** The ISO 2 country such as "US". */
  def isoCountry = {
    Option(activityContext.getSystemService(Context.TELEPHONY_SERVICE)).map(_.asInstanceOf[TelephonyManager]).
        flatMap(tm => Option(tm.getSimCountryIso)).getOrElse(java.util.Locale.getDefault.getCountry)
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
