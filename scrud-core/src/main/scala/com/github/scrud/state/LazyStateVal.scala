package com.github.scrud.state

/** Similar to StateVar but allows specifying an initial value, evaluated when first accessed. */
class LazyStateVal[T](lazyExpression: => T) {
  private val stateVar = new StateVar[T]

  /** Gets the value, evaluating if needed.
    * @param state the State where the value is stored
    * @return the value
    */
  def get(state: State): T = {
    stateVar.getOrSet(state, lazyExpression)
  }
}
