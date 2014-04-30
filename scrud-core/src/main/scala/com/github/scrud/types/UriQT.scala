package com.github.scrud.types

import com.netaporter.uri.Uri
import scala.util.Try

/**
 * A QualifiedType for a URI.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/13/14
 *         Time: 11:42 PM
 */
class UriQT extends StringConvertibleQT[Uri] {
  def convertFromString(string: String) = Try(Uri.parse(string))

  /** Convert the value to a String for editing.  This may simply call convertToString(value). */
  def convertToEditString(value: Uri) = convertToString(value)

  def convertToString(value: Uri) = value.toString()
}

object UriQT extends UriQT
