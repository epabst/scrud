package com.github.scrud.android.common

import collection.mutable.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._

/** A utility for interacting with threads, which enables overriding for testing.
  * @author Eric Pabst (epabst@gmail.com)
  */

trait Timing {
  def platformDriver: PlatformDriver

  private val workInProgress: ConcurrentMap[() => _,Unit] = new ConcurrentHashMap[() => _,Unit]()

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
    scala.concurrent.ops.future(trackWorkInProgress(propagateWithExceptionReporting(body))())
  }

  protected def toRunnable(operation: => Unit): Runnable = new Runnable {
    def run() {
      operation
    }
  }

  def trackWorkInProgress[T](body: => T): () => T = {
    val functionInProgress = new Function0[T]() {
      def apply() = try { body } finally { workInProgress.remove(this) }
    }
    workInProgress.put(functionInProgress, Unit)
    functionInProgress
  }

  def waitForWorkInProgress() {
    workInProgress.keys.foreach(_.apply())
  }
}

object Timing {
  def toRunnable(operation: => Unit): Runnable = new Runnable {
    def run() {
      operation
    }
  }
}