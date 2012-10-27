package com.github.scrud

import com.github.triangle.Logging

trait Notification extends Logging {
  /**
   * Display a message to the user temporarily.
   * @param message the message to display
   */
  def displayMessageToUser(message: String)

  /**
   * Handle the exception by communicating it to the user and developers.
   */
  def reportError(throwable: Throwable) {
    logError("Error in " + Thread.currentThread() + " Thread", throwable)

    // Also let the user know something went wrong
    val message = "Error: " + throwable.getClass.getSimpleName + " " + throwable.getMessage
    displayMessageToUser(message)
  }

  def propagateWithExceptionReporting[T](body: => T): T = {
    try {
      body
    } catch {
      case e: Throwable =>
        reportError(e)
        throw e
    }
  }

  def withExceptionReporting(body: => Unit) {
    try {
      body
    } catch {
      case e: Throwable =>
        reportError(e)
    }
  }

  def withExceptionReportingHavingDefaultReturnValue[T](exceptionalReturnValue: => T)(body: => T): T = {
    try {
      body
    } catch {
      case e: Throwable =>
        reportError(e)
        exceptionalReturnValue
    }
  }
}
