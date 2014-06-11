package com.github.scrud.util

/** A trait to enable logging via a delegate
  * @author Eric Pabst (epabst@gmail.com)
  */

trait DelegateLogging {
  protected def loggingDelegate: ExternalLogging

  def trace(f: => String) = loggingDelegate.trace(f)

  def debug(f: => String) = loggingDelegate.debug(f)

  def info(f: => String) = loggingDelegate.info(f)

  def warn(f: => String) = loggingDelegate.warn(f)

  def logError(f: => String) = loggingDelegate.logError(f)

  def logError(f: => String, e: Throwable) = loggingDelegate.logError(f, e)
}
