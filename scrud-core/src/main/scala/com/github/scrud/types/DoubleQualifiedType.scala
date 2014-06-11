package com.github.scrud.types

import scala.util.Try

/**
 * A QualifiedType for that holds a Double.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 5/22/14
 * Time: 4:47 PM
 */
abstract class DoubleQualifiedType extends StringConvertibleQT[Double] {
  /** Convert the value to a String for display. */
  def convertToDisplayString(value: Double) = value.toString

  /** Convert the value to a String for editing.  This may simply call convertToString(value). */
  def convertToString(value: Double) = value.toString

  /** Convert the value from a String (whether for editing or display. */
  def convertFromString(string: String) = Try(string.toDouble)
}
