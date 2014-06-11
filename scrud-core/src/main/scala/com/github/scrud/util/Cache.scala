package com.github.scrud.util

import scala.collection.concurrent

/** A cache of results.
  * @author Eric Pabst (epabst@gmail.com)
  */
class Cache {
  private val resultByInput: concurrent.Map[Any,Any] = concurrent.TrieMap[Any,Any]()

  def cacheBasedOn[T](args: Any*)(f: => T)(implicit manifest: Manifest[T]): T = {
    val cacheKey = manifest +: args
    resultByInput.get(cacheKey).asInstanceOf[Option[T]].getOrElse {
      val result: T = f
      resultByInput.putIfAbsent(cacheKey, result)
      result
    }
  }

  def setResult[T](result: Any, args: Any*)(implicit manifest: Manifest[T]) {
    val cacheKey = manifest +: args
    resultByInput.put(cacheKey, result)
  }

  def clear() {
    resultByInput.clear()
  }
}
