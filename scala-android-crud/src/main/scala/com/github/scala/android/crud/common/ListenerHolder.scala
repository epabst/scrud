package com.github.scala.android.crud.common

import java.util.concurrent.ConcurrentHashMap
import collection.mutable.ConcurrentMap
import scala.collection.JavaConversions._
import scala.collection.Set

trait ListenerSet[L] {
  def listeners: Set[L]
}

/** A Listener holder
  * @author Eric Pabst (epabst@gmail.com)
  */

trait ListenerHolder[L] {
  protected def listenerSet: ListenerSet[L]

  def listeners = listenerSet.listeners

  def addListener(listener: L)

  def removeListener(listener: L)
}

trait DelegatingListenerHolder[L] extends ListenerHolder[L] {
  protected def listenerHolder: ListenerHolder[L]

  protected def listenerSet = listenerHolder.listenerSet

  def addListener(listener: L) {
    listenerHolder.addListener(listener)
  }

  def removeListener(listener: L) {
    listenerHolder.removeListener(listener)
  }
}

private object UnsupportedListenerSet {
  def apply[L](): ListenerSet[L] = new ListenerSet[L] {
    def listeners: Set[L] = {
      throw new UnsupportedOperationException("listeners are not supported for " + this)
    }
  }
}

trait UnsupportedListenerHolder[L] extends ListenerHolder[L] with ListenerSet[L] {
  protected def listenerSet = UnsupportedListenerSet()

  def addListener(listener: L) {
    throw new UnsupportedOperationException("listeners are not supported for " + this)
  }

  def removeListener(listener: L) {
    throw new UnsupportedOperationException("listeners are not supported for " + this)
  }
}

class MutableListenerSet[L] extends ListenerSet[L] with ListenerHolder[L] {
  private val theListeners: ConcurrentMap[L,L] = new ConcurrentHashMap[L,L]()

  protected val listenerSet: ListenerSet[L] = new ListenerSet[L] {
    def listeners = theListeners.keySet
  }

  def addListener(listener: L) {
    theListeners += listener -> listener
  }

  def removeListener(listener: L) {
    theListeners -= listener
  }
}

trait SimpleListenerHolder[L] extends DelegatingListenerHolder[L] {
  protected lazy val listenerHolder: MutableListenerSet[L] = new MutableListenerSet[L]
}
