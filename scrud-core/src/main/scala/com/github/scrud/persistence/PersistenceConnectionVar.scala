package com.github.scrud.persistence

import com.github.scrud.state.{StateHolder, StateHolderVar}

/**
 * A variable whose value is stored per PersistenceConnection.
 * Created by eric on 6/2/14.
 */
class PersistenceConnectionVar[T] extends StateHolderVar[T] {
  protected def state(stateHolder: StateHolder) = stateHolder.asInstanceOf[PersistenceConnection].state
}
