package com.github.scrud.converter

import java.util.{Calendar, Date}
import Converter._
import GenericConverter._
import java.text.{SimpleDateFormat, Format}
import scala.util.Try

/** A value String format.
  * @author Eric Pabst (epabst@gmail.com)
  */
trait ValueFormat[T] {
  def toValue(s: String): Try[T]

  /** May need to be overridden */
  def toString(value: T): String = value.toString
}

object ValueFormat {
  def apply[T](toValue: String => Try[T], toString: T => String) = {
    val _toValue = toValue
    val _toString = toString
    new ValueFormat[T] {
      def toValue(s: String) = _toValue(s)

      override def toString(value: T) = _toString(value)
    }
  }

  def convertingFormat[T](toValueConverter: Converter[String,T], toStringConverter: Converter[T,String] = anyToString) =
    new ConvertingValueFormat[T](toValueConverter, toStringConverter)

  def toCalendarFormat(format: ValueFormat[Date]): ValueFormat[Calendar] = new ValueFormat[Calendar] {
    def toValue(s: String) = format.toValue(s).map(dateToCalendar.convert(_).get)

    override def toString(value: Calendar) = format.toString(calendarToDate.convert(value).get)
  }

  def textValueFormat[T](formatThreadLocal: ThreadLocal[_ <: Format], obj2Value: (Object) => T = {(v: Object) => v.asInstanceOf[T]}): ValueFormat[T] =
    convertingFormat[T](new ParseFormatConverter[T](formatThreadLocal, obj2Value), formatToString[T](formatThreadLocal))

  def basicFormat[T <: AnyVal](implicit manifest: Manifest[T]): ValueFormat[T] =
    new GenericConvertingValueFormat[T](stringToAnyVal, anyToString)

  lazy val currencyValueFormat = convertingFormat(stringToCurrency, currencyToEditString)
  lazy val currencyDisplayValueFormat = convertingFormat(stringToCurrency, currencyToString)
  lazy val dateValueFormat = convertingFormat(stringToDate, dateToString)
  lazy val dateDisplayValueFormat = convertingFormat(stringToDate, dateToDisplayString)
  lazy val calendarValueFormat = toCalendarFormat(dateValueFormat)
  lazy val calendarDisplayValueFormat = toCalendarFormat(dateDisplayValueFormat)
  lazy val persistedDateFormat = textValueFormat[Date](toThreadLocal(new SimpleDateFormat("yyyy-MM-dd")))
}
