package com.github.scrud.state

import collection.mutable
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._

/**
 * A [[com.github.scrud.state.LazyApplicationVal]] with a ConcurrentMap.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/6/13
 * Time: 5:11 PM
 */
class ApplicationConcurrentMapVal[K,V] extends LazyApplicationVal[mutable.ConcurrentMap[K,V]](new ConcurrentHashMap[K,V]())
