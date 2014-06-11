package com.github.scrud.util

/** A trait to enable logging
  * @author Eric Pabst (epabst@gmail.com)
  */

trait ExternalLogging extends Logging {
  override def trace(f: => String) = super.trace(f)

  override def debug(f: => String) = super.debug(f)

  override def info(f: => String) = super.info(f)

  override def warn(f: => String) = super.warn(f)

  override def logError(f: => String) = super.logError(f)

  override def logError(f: => String, e: Throwable) = super.logError(f, e)
}
