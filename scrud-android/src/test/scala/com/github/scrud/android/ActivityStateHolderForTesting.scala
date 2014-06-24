package com.github.scrud.android

import com.github.scrud.state.State
import com.github.scrud.android.state.ActivityStateHolder
import android.app.Activity

/**
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 6/24/14
 */
class ActivityStateHolderForTesting extends Activity with ActivityStateHolder {
  val activityState = new State
  val applicationState = new State
}
