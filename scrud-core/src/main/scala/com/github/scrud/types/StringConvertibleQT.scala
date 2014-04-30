package com.github.scrud.types

import scala.util.Try

/**
 * A [[com.github.scrud.types.QualifiedType]] that can be converted to/from a String.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/31/14
 *         Time: 3:18 PM
 */
trait StringConvertibleQT[V] extends QualifiedType[V] {
  /** Convert the value to a String for display. */
  def convertToString(value: V): String

  /** Convert the value to a String for editing.  This may simply call convertToString(value). */
  def convertToEditString(value: V): String

  /** Convert the value from a String (whether for editing or display. */
  def convertFromString(string: String): Try[V]
}
