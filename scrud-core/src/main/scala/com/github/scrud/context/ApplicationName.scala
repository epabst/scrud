package com.github.scrud.context

import com.github.scrud.util.{ExternalLogging, Name}

/**
 * The name of an application.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/27/14
 *         Time: 11:43 PM
 */
case class ApplicationName(name: String) extends Name with ExternalLogging {
  def logTag = toSnakeCase
}
