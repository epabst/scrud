package com.github.scrud.state

import com.github.scrud.util.SimpleListenerHolder
import scala.collection.concurrent

/** A container for values of [[com.github.scrud.state.StateVar]]'s */
class State extends SimpleListenerHolder[DestroyStateListener] {
  //for some reason, making this lazy results in it being null during testing, even though lazy would be preferrable.
  private[state] val variables: concurrent.Map[StateVar[_], Any] = concurrent.TrieMap[StateVar[_], Any]()

  def onDestroyState() {
    listeners.foreach(_.onDestroyState())
    listeners.foreach(removeListener(_))
    variables.clear()
  }
}
