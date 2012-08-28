package com.github.scrud.android.common

import com.github.triangle.Logging

/** A utility for interacting with threads, which enables overriding for testing.
  * @author Eric Pabst (epabst@gmail.com)
  */

trait Timing extends Logging {
  def platformDriver: PlatformDriver

  def propagateWithExceptionReporting[T](body: => T): T = {
    try {
      body
    } catch {
      case e: Throwable =>
        platformDriver.reportError(e)
        throw e
    }
  }

  def withExceptionReporting(body: => Unit) {
    try {
      body
    } catch {
      case e: Throwable =>
        platformDriver.reportError(e)
    }
  }

  def future[T](body: => T): () => T = {
    // Use this instead of scala.actors.Futures.future because it preserves exceptions
    scala.concurrent.ops.future(propagateWithExceptionReporting(body))
  }

  protected def toRunnable(operation: => Unit): Runnable = new Runnable {
    def run() {
      operation
    }
  }
}
