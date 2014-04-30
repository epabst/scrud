package com.github.scrud.types

import scala.util.Success

/**
 * A QualifiedType that holds a String.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/22/13
 * Time: 4:47 PM
 */
abstract class StringQualifiedType extends QualifiedType[String] with StringConvertibleQT[String] {
  /** Convert the value to a String for display. */
  def convertToString(value: String) = value

  /** Convert the value to a String for editing.  This may simply call convertToString(value). */
  def convertToEditString(value: String) = value

  /** Convert the value from a String (whether for editing or display. */
  def convertFromString(string: String) = Success(string)
}
