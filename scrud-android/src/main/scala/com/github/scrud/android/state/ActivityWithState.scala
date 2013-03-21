package com.github.scrud.android.state

import android.app.Activity
import com.github.scrud.state.State
import com.github.scrud.android.CrudAndroidApplication

/**
 * The state for an Android Activity mixed in with the Activity itself.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/21/13
 * Time: 5:32 PM
 */
trait ActivityWithState extends Activity with ActivityStateHolder {
  // getApplication gets the Android application, which should extend CrudAndroidApplication
  lazy val applicationState: State = getApplication.asInstanceOf[CrudAndroidApplication].applicationState

  lazy val activityState = new State
}
