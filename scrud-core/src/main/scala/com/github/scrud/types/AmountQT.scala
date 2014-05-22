package com.github.scrud.types

import scala.util.Try

/**
 * A QualifiedType for an amount of something such as a temperature, expressed as a positive or negative number.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/11/14
 * Time: 3:47 PM
 */
object AmountQT extends StringConvertibleQT[Double] {
  /** Convert the value to a String for display. */
  def convertToDisplayString(value: Double) = value.toString

  /** Convert the value to a String for editing.  This may simply call convertToString(value). */
  def convertToString(value: Double) = value.toString

  /** Convert the value from a String (whether for editing or display. */
  def convertFromString(string: String) = Try(string.toDouble)
}
