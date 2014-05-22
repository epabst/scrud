package com.github.scrud.types

import scala.util.Try

/**
 * A [[com.github.scrud.types.QualifiedType]] that can be converted to/from a String.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/31/14
 *         Time: 3:18 PM
 */
trait StringConvertibleQT[V] extends QualifiedType[V] {
  /** Convert the value to a String for display.  This may simply call convertToString(value). */
  def convertToDisplayString(value: V): String

  /** Convert the value to a String for editing/persisting. */
  def convertToString(value: V): String

  /** Convert the value from a String for editing/persisting/displaying.  Support for parsing display strings might not be needed. */
  def convertFromString(string: String): Try[V]
}
