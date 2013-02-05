package com.github.scrud.util

import java.util.concurrent.ConcurrentHashMap
import collection.mutable
import scala.collection.JavaConversions._
import scala.collection.Set

/** A Listener holder
  * @author Eric Pabst (epabst@gmail.com)
  */

trait ListenerHolder[L] extends ListenerSet[L] {
  protected def listenerSet: ListenerSet[L]

  def listeners = listenerSet.listeners

  def addListener(listener: L)

  def addListenerIfNotPresent(listener: L): Boolean = {
    if (!listeners.contains(listener)) {
      addListener(listener)
      true
    } else false
  }

  def removeListener(listener: L)
}

trait ListenerSet[L] {
  def listeners: Set[L]
}

trait DelegatingListenerSet[L] extends ListenerSet[L] {
  protected def listenerSet: ListenerSet[L]

  def listeners: Set[L] = listenerSet.listeners
}

trait DelegatingListenerHolder[L] extends ListenerHolder[L] {
  protected def listenerHolder: ListenerHolder[L]

  protected def listenerSet = listenerHolder

  def addListener(listener: L) {
    listenerHolder.addListener(listener)
  }

  def removeListener(listener: L) {
    listenerHolder.removeListener(listener)
  }
}

class MutableListenerSet[L] extends ListenerSet[L] with ListenerHolder[L] {
  private val theListeners: mutable.ConcurrentMap[L,L] = new ConcurrentHashMap[L,L]()

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
