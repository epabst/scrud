package com.github.scrud.types

import scala.util.Try

/**
 * A QualifiedType for that holds an Int.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/22/13
 * Time: 4:47 PM
 */
abstract class IntQualifiedType extends StringConvertibleQT[Int] {
  /** Convert the value to a String for display. */
  def convertToDisplayString(value: Int) = convertToString(value)

  /** Convert the value to a String for editing.  This may simply call convertToString(value). */
  def convertToString(value: Int) = value.toString

  /** Convert the value from a String (whether for editing or display. */
  def convertFromString(string: String) = Try(string.toInt)
}
