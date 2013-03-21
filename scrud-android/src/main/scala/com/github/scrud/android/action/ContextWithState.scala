package com.github.scrud.android.action

import android.content.Context
import android.app.Activity
import com.github.scrud.state.State
import android.widget.Toast
import com.github.scrud.util.Common
import com.github.scrud.Notification
import com.github.scrud.platform.PlatformTypes
import com.github.scrud.android.state.ActivityStateHolder

/**
 * The state for an Android context which has a reference to the application state as well.
 * A State that has been mixed with a Context.
 * @author Eric Pabst (epabst@gmail.com)
 */
trait ContextWithState extends Context with State

/** The state for an Android Activity mixed in with the Activity itself. */
trait ActivityWithState extends Activity with ContextWithState with ActivityStateHolder {
  // getApplication gets the Android application, which should extend CrudAndroidApplication, which already extends State.
  def applicationState: State = getApplication.asInstanceOf[State]

  def activityState = this
}

trait AndroidNotification extends Notification {
  def context: Context

  def displayMessageToUser(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
  }

  /**
   * Display a message to the user temporarily.
   * @param messageKey the key of the message to display
   */
  def displayMessageToUserBriefly(messageKey: PlatformTypes.SKey) {
    Toast.makeText(context, messageKey, Toast.LENGTH_SHORT).show()
  }

  def runOnUiThread[T](body: => T) {
    context.asInstanceOf[Activity].runOnUiThread(Common.toRunnable(withExceptionReporting(body)))
  }
}
