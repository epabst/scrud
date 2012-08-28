package com.github.scrud.android.common

/** A utility for interacting with threads, which enables overriding for testing.
  * @author Eric Pabst (epabst@gmail.com)
  */

trait Timing {
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

  def withExceptionReportingHavingDefaultReturnValue[T](exceptionalReturnValue: => T)(body: => T): T = {
    try {
      body
    } catch {
      case e: Throwable =>
        platformDriver.reportError(e)
        exceptionalReturnValue
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
