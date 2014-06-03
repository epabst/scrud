package com.github.scrud.types

import com.github.scrud.converter.Converter

/**
 * A QualifiedType for a percentage.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/22/13
 * Time: 4:47 PM
 */
object PercentageQT extends FloatQualifiedType {
  /** Convert the value to a String for display. */
  def convertToDisplayString(value: Float) = Converter.percentageToString.convert(value).get

  /** Convert the value to a String for editing.  This may simply call convertToString(value). */
  def convertToString(value: Float) = Converter.percentageToEditString.convert(value).get

  /** Convert the value from a String (whether for editing or display. */
  def convertFromString(string: String) = Converter.stringToPercentage.convert(string)
}
