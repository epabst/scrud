package com.github.scrud.android.action

import android.content.Context
import android.app.Activity
import com.github.scrud.state.State
import android.widget.Toast
import com.github.scrud.util.Common
import com.github.scrud.Notification

/**
 * The state for an Android context which has a reference to the application state as well.
 * A State that has been mixed with a Context.
 * @author Eric Pabst (epabst@gmail.com)
 */
trait ContextWithState extends Context with State {
  def applicationState: State
}

/** The state for an Android Activity mixed in with the Activity itself. */
trait ActivityWithState extends Activity with ContextWithState {
  // getApplication gets the Android application, which should extend CrudAndroidApplication, which already extends State.
  def applicationState: State = getApplication.asInstanceOf[State]
}

trait AndroidNotification extends Notification {
  def activityContext: ContextWithState

  def displayMessageToUser(message: String) {
    Toast.makeText(activityContext, message, Toast.LENGTH_LONG).show()
  }

  def runOnUiThread[T](body: => T) {
    activityContext.asInstanceOf[Activity].runOnUiThread(Common.toRunnable(withExceptionReporting(body)))
  }
}
