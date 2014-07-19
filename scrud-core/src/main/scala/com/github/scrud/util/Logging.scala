package com.github.scrud.util

import org.slf4j.LoggerFactory

/** A trait to enable logging
  * @author Eric Pabst (epabst@gmail.com)
  */

trait Logging {
  protected def logTag: String
  lazy val logger = LoggerFactory.getLogger(logTag)

  protected def trace(f: => String) {
    if (logger.isTraceEnabled) logger.trace(f)
  }

  protected def debug(f: => String) {
    if (logger.isDebugEnabled) logger.debug(f)
  }

  protected def info(f: => String) {
    if (logger.isInfoEnabled) logger.info(f)
  }

  protected def warn(f: => String) {
    if (logger.isWarnEnabled) logger.warn(f)
  }

  protected def logError(f: => String) {
    if (logger.isErrorEnabled) logger.error(f)
  }

  protected def logError(f: => String, e: Throwable) {
    if (logger.isErrorEnabled) logger.error(f, e)
  }
}
