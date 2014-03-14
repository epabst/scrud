package com.github.scrud.types

import com.netaporter.uri.Uri
import scala.util.Try

/**
 * A QualifiedType for an Image.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/13/14
 *         Time: 11:42 PM
 */
object ImageQT extends StringConvertibleQT[Uri] {
  override def convertFromString(string: String) = Try(Uri.parse(string))

  override def convertToString(value: Uri) = value.toString()
}
