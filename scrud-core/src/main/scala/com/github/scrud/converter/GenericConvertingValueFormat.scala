package com.github.scrud.converter

/**
 * A ValueFormat that uses a GenericConverter (and a Converter to convert to a String).
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/22/13
 *         Time: 7:48 AM
 */
class GenericConvertingValueFormat[T](toValueConverter: GenericConverter[String,T],
                                      toStringConverter: Converter[T, String] = Converter.anyToString)(implicit manifest: Manifest[T])
        extends ValueFormat[T] {
  def toValue(s: String) = toValueConverter.convertTo[T](s)

  override def toString(value: T) = toStringConverter.convert(value).getOrElse(throw new IllegalArgumentException(String.valueOf(value)))
}
