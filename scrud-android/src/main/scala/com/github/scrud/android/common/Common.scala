package com.github.scrud.android.common

/** Common functionality that is used throughout scrud.
  * @author Eric Pabst (epabst@gmail.com)
  */

object Common {
  val logTag = "scrud"

  /** Evaluates the given function and returns the result.  If it throws an exception, it returns None. */
  def tryToEvaluate[T](f: => T): Option[T] = {
    try { Option(f) }
    catch { case _: Throwable => None }
  }

  def withCloseable[C <: {def close()},T](closeable: C)(f: C => T): T = {
    try { f(closeable) }
    finally { closeable.close() }
  }
}