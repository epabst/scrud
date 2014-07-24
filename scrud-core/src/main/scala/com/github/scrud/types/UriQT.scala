package com.github.scrud.types

import java.net.URI
import scala.util.Try

/**
 * A QualifiedType for a URI.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/13/14
 *         Time: 11:42 PM
 */
class UriQT extends StringConvertibleQT[URI] {
  def convertFromString(string: String) = Try(URI.create(string))

  /** Convert the value to a String for editing. */
  def convertToString(value: URI) = value.toString

  def convertToDisplayString(value: URI) = convertToString(value)
}

object UriQT extends UriQT
