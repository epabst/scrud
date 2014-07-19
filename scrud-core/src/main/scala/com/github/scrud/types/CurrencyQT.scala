package com.github.scrud.types

import com.github.scrud.converter.Converter

/**
 * A QualifiedType for a currency amount.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/22/13
 * Time: 4:47 PM
 */
object CurrencyQT extends DoubleQualifiedType {
  /** Convert the value to a String for display. */
  override def convertToDisplayString(value: Double) = Converter.currencyToString.convert(value).get

  /** Convert the value to a String for editing. */
  override def convertToString(value: Double) = Converter.currencyToEditString.convert(value).get

  /** Convert the value from a String (whether for editing or display. */
  override def convertFromString(string: String) = Converter.stringToCurrency.convert(string)
}
