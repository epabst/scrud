package com.github.scrud.state

/** Similar to ApplicationVar but allows specifying an initial value, evaluated when first accessed. */
class LazyApplicationVal[T](lazyExpression: => T) {
  private val applicationVar = new ApplicationVar[T]

  /** Gets the value, evaluating if needed.
    * @param stateHolder the StateHolder where the value is stored
    * @return the value
    */
  def get(stateHolder: StateHolder): T = {
    applicationVar.getOrSet(stateHolder, lazyExpression)
  }
}
