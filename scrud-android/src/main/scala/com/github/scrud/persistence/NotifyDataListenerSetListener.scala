package com.github.scrud.persistence

import com.github.scrud.util.ListenerSet
import com.github.scrud.UriPath

/**
 * This is a top-level case class so that it can be identified correctly to avoid re-adding it and for removal.
 * It is identified by which ListenerSet it contains.
 */
case class NotifyDataListenerSetListener(listenerSet: ListenerSet[DataListener]) extends DataListener {
  def onChanged(uri: UriPath) {
    listenerSet.listeners.foreach(_.onChanged(uri))
  }
}
