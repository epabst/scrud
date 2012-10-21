package com.github.scrud.android.action

import android.content.Context
import android.app.Activity
import com.github.scrud.state.State

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
