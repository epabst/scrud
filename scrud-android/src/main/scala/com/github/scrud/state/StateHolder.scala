package com.github.scrud.state

/**
 * A trait for something that has (possibly various types of) State.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/5/13
 * Time: 3:21 PM
 */
trait StateHolder {
  def applicationState: State
}
