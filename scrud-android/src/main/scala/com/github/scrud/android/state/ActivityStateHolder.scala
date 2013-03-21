package com.github.scrud.android.state

import com.github.scrud.state.{State, StateHolder}

/**
 * A StateHolder that also has State for an Activity or Service.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/21/13
 * Time: 4:19 PM
 */
trait ActivityStateHolder extends StateHolder {
  def activityState: State
}
