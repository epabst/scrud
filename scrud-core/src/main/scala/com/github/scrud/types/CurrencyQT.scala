package com.github.scrud.types

import com.github.scrud.converter.Converter

/**
 * A QualifiedType for a currency amount.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/22/13
 * Time: 4:47 PM
 */
object CurrencyQT extends StringConvertibleQT[Double] {
  /** Convert the value to a String for display. */
  def convertToString(value: Double) = Converter.currencyToString.convert(value).get

  /** Convert the value to a String for editing. */
  override def convertToEditString(value: Double) = Converter.currencyToEditString.convert(value).get

  /** Convert the value from a String (whether for editing or display. */
  def convertFromString(string: String) = Converter.stringToCurrency.convert(string)
}
