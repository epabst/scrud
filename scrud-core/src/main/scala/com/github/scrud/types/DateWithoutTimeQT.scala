package com.github.scrud.types

import java.util.Date
import com.github.scrud.converter.Converter

/**
 * A QualifiedType for a Date that does not include the time on the given day.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/22/13
 * Time: 4:47 PM
 */
object DateWithoutTimeQT extends StringConvertibleQT[Date] {
  /** Convert the value to a String for display. */
  def convertToDisplayString(value: Date) = Converter.dateToDisplayString.convert(value).get

  /** Convert the value to a String for editing.  This may simply call [[com.github.scrud.types.StringConvertibleQT.convertToDisplayString( )]] */
  def convertToString(value: Date) = Converter.dateToString.convert(value).get

  /** Convert the value from a String (whether for editing or display. */
  def convertFromString(string: String) = Converter.stringToDate.convert(string)
}
