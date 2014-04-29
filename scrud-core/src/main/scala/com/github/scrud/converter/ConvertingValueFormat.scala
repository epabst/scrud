package com.github.scrud.converter

/**
 * A ValueFormat that uses a Converter.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/22/13
 *         Time: 7:48 AM
 */
class ConvertingValueFormat[T](toValueConverter: Converter[String,T],
                               toStringConverter: Converter[T,String] = Converter.anyToString) extends ValueFormat[T] {
  def toValue(s: String) = toValueConverter.convert(s)

  override def toString(value: T) = toStringConverter.convert(value).getOrElse(throw new IllegalArgumentException(String.valueOf(value)))
}
