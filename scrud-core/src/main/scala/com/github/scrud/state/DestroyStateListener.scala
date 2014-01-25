package com.github.scrud.state

/** A listener for when a StateHolder is being destroyed and resources should be released. */
trait DestroyStateListener {
  def onDestroyState()
}
