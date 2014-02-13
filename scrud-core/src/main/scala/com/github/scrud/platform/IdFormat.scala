package com.github.scrud.platform

import com.github.scrud.platform.PlatformTypes.ID
import scala.util.Try

/**
 * A format of an ID to and from a String.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/24/14
 *         Time: 9:55 PM
 */
object IdFormat {
  def toString(id: ID): String = id.toString

  def toValue(idString: String): Try[ID] = Try(idString.toLong)
}
