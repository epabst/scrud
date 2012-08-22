package com.github.scrud.android.common

import android.view.View
import android.app.Activity
import com.github.triangle.Logging

/** A utility for interacting with threads, which enables overriding for testing.
  * @author Eric Pabst (epabst@gmail.com)
  */

trait Timing extends Logging {
  private def withExceptionLogging[T](body: => T): T = {
    try {
      body
    } catch {
      case e: Throwable =>
        logError("Error in non-UI Thread", e)
        throw e
    }
  }

  def future[T](body: => T): () => T = {
    // Use this instead of scala.actors.Futures.future because it preserves exceptions
    scala.concurrent.ops.future(withExceptionLogging(body))
  }

  def runOnUiThread[T](view: View)(body: => T) {
    view.post(toRunnable(withExceptionLogging(body)))
  }

  def runOnUiThread[T](activity: Activity)(body: => T) {
    activity.runOnUiThread(toRunnable(withExceptionLogging(body)))
  }

  private def toRunnable(operation: => Unit): Runnable = new Runnable {
    def run() {
      operation
    }
  }
}
