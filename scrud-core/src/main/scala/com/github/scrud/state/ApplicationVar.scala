package com.github.scrud.state

/** A variable whose value is stored on a per-application basis in StateHolder. */
class ApplicationVar[T] extends StateHolderVar[T] {
  protected def state(stateHolder: StateHolder) = stateHolder.applicationState
}
