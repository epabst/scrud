package com.github.scrud.state

/** A variable stored in a [[com.github.scrud.state.State]].
  * <p />
  * Normally you create an object that extends this:
  * {{{object ProductName extends StateVar[String]}}}
  * But if you need uniqueness by instance, do this:
  * {{{val productName = new StateVar[String]}}}
  * It doesn't accumulate any data and is sharable across threads since all data is stored in each State instance.
  */
class StateVar[T] {
  /** Gets the value or None if not set.
    * @param state the State where the value is stored
    * @return Some(value) if set, otherwise None
    */
  def get(state: State): Option[T] = {
    state.variables.get(this).map(_.asInstanceOf[T])
  }

  /** Tries to set the value in {{{state}}}.
    * @param state the State where the value is stored
    * @param value the value to set in the State.
    */
  def set(state: State, value: T) {
    state.variables.put(this, value)
  }

  def clear(state: State): Option[T] = {
    state.variables.remove(this).map(_.asInstanceOf[T])
  }

  def getOrSet(state: State, initialValue: => T): T = {
    get(state).getOrElse {
      val value = initialValue
      set(state, value)
      value
    }
  }
}
