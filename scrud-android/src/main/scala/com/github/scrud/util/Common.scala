package com.github.scrud.util

import java.io.Closeable

/** Common functionality that is used throughout scrud.
  * @author Eric Pabst (epabst@gmail.com)
  */

object Common {
  val logTag = "scrud"

  /** Evaluates the given function and returns the result.  If it throws an exception, it returns None. */
  def tryToEvaluate[T](f: => T): Option[T] = evaluateOrIntercept(f).left.toOption

  /** Evaluates the given function and returns the result or the exception. */
  def evaluateOrIntercept[T](f: => T): Either[T,Throwable] = {
    try { Left(f) }
    catch {
      case exception: Throwable =>
        Right(exception)
    }
  }

  def withCloseable[C <: Closeable,T](closeable: C)(f: C => T): T = {
    try { f(closeable) }
    finally { closeable.close() }
  }

  def toRunnable(operation: => Unit): Runnable = new Runnable {
    def run() {
      operation
    }
  }
}
