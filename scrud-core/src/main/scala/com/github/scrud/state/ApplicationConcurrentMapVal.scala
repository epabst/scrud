package com.github.scrud.state

import scala.collection.concurrent

/**
 * A [[com.github.scrud.state.LazyApplicationVal]] with a ConcurrentMap.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/6/13
 * Time: 5:11 PM
 */
class ApplicationConcurrentMapVal[K,V] extends LazyApplicationVal[concurrent.Map[K,V]](concurrent.TrieMap[K,V]())
