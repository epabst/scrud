package com.github.scrud.state

/**
 * A [[com.github.scrud.state.StateHolder]] for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/30/13
 * Time: 10:33 PM
 */
class StateHolderForTesting extends StateHolder {
  val applicationState = new State
}
