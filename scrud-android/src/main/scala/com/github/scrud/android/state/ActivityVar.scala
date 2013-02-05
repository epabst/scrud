package com.github.scrud.android.state

import com.github.scrud.state.{StateHolder, StateHolderVar}
import com.github.scrud.android.AndroidCrudContext

/** A variable whose value is stored on a per-activity basis in an AndroidCrudContext. */
class ActivityVar[T] extends StateHolderVar[T] {
  protected def state(stateHolder: StateHolder) = stateHolder.asInstanceOf[AndroidCrudContext].activityState
}
