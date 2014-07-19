package com.github.scrud.util

import java.io.Closeable

/** Common functionality that is used throughout scrud.
  * @author Eric Pabst (epabst@gmail.com)
  */

object Common {
  val logTag = "scrud"

  def withCloseable[C <: Closeable,T](closeable: C)(f: C => T): T = {
    try { f(closeable) }
    finally { closeable.close() }
  }

  def toRunnable(operation: => Unit): Runnable = new Runnable {
    def run() {
      operation
    }
  }

  def toRunnable(name: String, logging: ExternalLogging)(operation: => Unit): Runnable = toRunnable {
    if (Debug.threading) logging.debug(s"Running $name")
    try {
      operation
    } finally {
      if (Debug.threading) logging.debug(s"Done running $name")
    }
  }
}
