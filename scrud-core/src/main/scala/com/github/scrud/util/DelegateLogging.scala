package com.github.scrud.util

/** A trait to enable logging via a delegate
  * @author Eric Pabst (epabst@gmail.com)
  */

trait DelegateLogging {
  protected def loggingDelegate: ExternalLogging

  protected def trace(f: => String) = loggingDelegate.trace(f)

  protected def debug(f: => String) = loggingDelegate.debug(f)

  protected def info(f: => String) = loggingDelegate.info(f)

  protected def warn(f: => String) = loggingDelegate.warn(f)

  protected def logError(f: => String) = loggingDelegate.logError(f)

  protected def logError(f: => String, e: Throwable) = loggingDelegate.logError(f, e)
}
