package com.github.scrud.state

/**
 * A variable stored by StateHolder.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:15 PM
 */
abstract class StateHolderVar[T] {
  private val stateVar: StateVar[T] = new StateVar[T]

  protected def state(stateHolder: StateHolder): State

  def get(stateHolder: StateHolder): Option[T] = stateVar.get(state(stateHolder))

  /** Tries to set the value in {{{stateHolder}}}.
    * @param stateHolder the StateHolder where the value is stored
    * @param value the value to set in the StateHolder.
    */
  def set(stateHolder: StateHolder, value: T) {
    stateVar.set(state(stateHolder), value)
  }

  def clear(stateHolder: StateHolder): Option[T] = {
    stateVar.clear(state(stateHolder))
  }

  def getOrSet(stateHolder: StateHolder, initialValue: => T): T = {
    stateVar.getOrSet(state(stateHolder), initialValue)
  }
}
