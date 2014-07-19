package com.github.scrud.android.state

import com.github.scrud.state.{StateHolder, StateHolderVar}

/** A variable whose value is stored on a per-activity basis in an ActivityStateHolder. */
class ActivityVar[T] extends StateHolderVar[T] {
  protected def state(stateHolder: StateHolder) = stateHolder.asInstanceOf[ActivityStateHolder].activityState
}
