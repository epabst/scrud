package com.github.scrud.state

import com.github.scrud.util.MicrotestCompatible

/**
 * A trait for something that has (possibly various types of) State.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/5/13
 * Time: 3:21 PM
 */
@MicrotestCompatible(use = "new SimpleStateHolder")
trait StateHolder {
  def applicationState: State
}

class SimpleStateHolder extends StateHolder {
  val applicationState = new State
}