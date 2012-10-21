package com.github.scrud.state

import collection.mutable
import java.util.concurrent.ConcurrentHashMap
import collection.JavaConversions._
import com.github.scrud.util.SimpleListenerHolder

/** A container for values of [[com.github.scrud.state.StateVar]]'s */
trait State extends SimpleListenerHolder[DestroyStateListener] {
  //for some reason, making this lazy results in it being null during testing, even though lazy would be preferrable.
  private[scrud] val variables: mutable.ConcurrentMap[StateVar[_], Any] = new ConcurrentHashMap[StateVar[_], Any]()

  def onDestroyState() {
    listeners.foreach(_.onDestroyState())
  }
}
