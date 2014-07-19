package com.github.scrud.util

import scala.collection.concurrent

/** A cache of results of a function.
  * @author Eric Pabst (epabst@gmail.com)
  */
case class CachedFunction[A,B](function: (A) => B) extends ((A) => B) {
  private val resultByInput: concurrent.Map[A,B] = concurrent.TrieMap[A,B]()

  def apply(input: A): B = resultByInput.get(input).getOrElse {
    val result = function(input)
    resultByInput.putIfAbsent(input, result)
    result
  }

  def setResult(input: A, result: B) {
    resultByInput.put(input, result)
  }

  def clear() {
    resultByInput.clear()
  }
}
