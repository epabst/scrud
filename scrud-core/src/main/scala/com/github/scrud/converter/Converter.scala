package com.github.scrud.converter

import java.text.{DateFormat, Format, NumberFormat}
import java.util.{Calendar, Date}
import scala.util.{Failure, Success, Try}

/**
 * A converter from one type to another.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/22/13
 *         Time: 7:44 AM
 */
trait Converter[-A,+B] {
  /** Converts from {{{from}}} to the new type if possible. */
  def convert(from: A): Try[B]
}

object Converter {
  /**
   * This is a polymorphic singleton where a single instance is used to implement any number of types.
   * This is so that it can be used to identify when a Converter isn't doing anything and may be bypassed.
   */
  def identityConverter[A]: Converter[A,A] = _identityConverter.asInstanceOf[Converter[A,A]]

  private val _identityConverter = new Converter[Any,Any] {
    def convert(from: Any) = Success(from)
  }

  implicit def apply[A,B](f: A => Try[B]): Converter[A,B] = new Converter[A,B] {
    def convert(from: A) = f(from)
  }

  val anyToString: Converter[Any,String] = new Converter[Any,String] {
    def convert(from: Any) = Success(from.toString)
  }

  val noConverter: Converter[Any,Nothing] = new Converter[Any,Nothing] {
    def convert(from: Any) = Failure(new UnsupportedOperationException("Converter.noConverter can't convert anything"))
  }

  private lazy val defaultCurrencyFormatThreadLocal = toThreadLocal[NumberFormat](NumberFormat.getCurrencyInstance)

  private lazy val currencyFormat = toThreadLocal[Format] {
    val format = NumberFormat.getNumberInstance
    val defaultCurrencyFormat = defaultCurrencyFormatThreadLocal.get()
    format.setMinimumFractionDigits(defaultCurrencyFormat.getMinimumFractionDigits)
    format.setMaximumFractionDigits(defaultCurrencyFormat.getMaximumFractionDigits)
    format.setGroupingUsed(defaultCurrencyFormat.isGroupingUsed)
    format.setGroupingUsed(defaultCurrencyFormat.isGroupingUsed)
    format
  }
  private lazy val currencyEditFormat = toThreadLocal[Format] {
    val editFormat = NumberFormat.getNumberInstance
    val defaultCurrencyFormat = defaultCurrencyFormatThreadLocal.get()
    editFormat.setMinimumFractionDigits(defaultCurrencyFormat.getMinimumFractionDigits)
    editFormat.setMaximumFractionDigits(defaultCurrencyFormat.getMaximumFractionDigits)
    editFormat
  }

  val stringToCurrency: Converter[String,Double] = new CompositeDirectConverter[String,Double](
    List(currencyEditFormat, currencyFormat, toThreadLocal[Format](NumberFormat.getNumberInstance), defaultCurrencyFormatThreadLocal).map(
      (new ParseFormatConverter[Double](_, _.asInstanceOf[Number].doubleValue)))
  )

  private lazy val percentagePercentFormat: ThreadLocal[NumberFormat] = toThreadLocal(NumberFormat.getPercentInstance)
  private lazy val percentageEditNumberFormat = toThreadLocal {
    val editFormat = NumberFormat.getNumberInstance
    editFormat.setMinimumFractionDigits(percentagePercentFormat.get().getMinimumFractionDigits)
    editFormat.setMaximumFractionDigits(percentagePercentFormat.get().getMaximumFractionDigits)
    editFormat
  }
  val stringToPercentage: Converter[String,Float] = new CompositeDirectConverter[String,Float](
    new ParseFormatConverter[Float](percentagePercentFormat, _.asInstanceOf[Number].floatValue()) +:
      List(percentageEditNumberFormat, toThreadLocal(NumberFormat.getNumberInstance)).map(
        new ParseFormatConverter[Float](_, _.asInstanceOf[Number].floatValue() / 100))
  )
  lazy val percentageToEditString: Converter[Float,String] = new Converter[Float,String] {
    def convert(from: Float) = formatToString[Float](percentageEditNumberFormat).convert(from * 100)
  }
  lazy val percentageToString: Converter[Float,String] = new Converter[Float,String] {
    def convert(from: Float) = formatToString[Float](percentagePercentFormat).convert(from)
  }

  lazy val dateToLong = Converter[Date, Long](d => Success(d.getTime))
  lazy val longToDate = Converter[Long, Date](l => Success(new Date(l)))

  lazy val dateToCalendar = Converter[Date, Calendar] { date =>
    val calendar = Calendar.getInstance
    calendar.setTime(date)
    Success(calendar)
  }
  lazy val calendarToDate = Converter[Calendar, Date](c => Success(c.getTime))

  def formatToString[T](format: ThreadLocal[_ <: Format]): Converter[T,String] = Converter[T,String](value => Success(format.get().format(value)))

  lazy val currencyToString: Converter[Double,String] = formatToString[Double](currencyFormat)
  lazy val currencyToEditString: Converter[Double,String] = formatToString[Double](currencyEditFormat)

  private def threadLocalDateFormats = List(DateFormat.SHORT, DateFormat.DEFAULT, DateFormat.MEDIUM).map { dateFormatType =>
    toThreadLocal[Format](DateFormat.getDateInstance(dateFormatType))
  } ++ List("MM/dd/yyyy", "yyyy-MM-dd", "dd MMM yyyy").map(s => toThreadLocal[Format](new java.text.SimpleDateFormat(s)))

  private[converter] def toThreadLocal[T](factory: => T): ThreadLocal[T] = new ThreadLocal[T] {
    override def initialValue() = factory
  }

  lazy val stringToDate = new CompositeDirectConverter[String,Date](threadLocalDateFormats.map(new ParseFormatConverter[Date](_)))
  //SHORT is probably the best style for input
  lazy val dateToString = formatToString[Date](toThreadLocal(DateFormat.getDateInstance(DateFormat.SHORT)))
  //DEFAULT is probably the best style for output
  lazy val dateToDisplayString = formatToString[Date](toThreadLocal(DateFormat.getDateInstance(DateFormat.DEFAULT)))

  def stringToEnum[T <: Enumeration#Value](enumeration: Enumeration): Converter[String,T] = new Converter[String,T] {
    def convert(from: String) = Try(enumeration.values.find(_.toString == from).map(_.asInstanceOf[T]).
      getOrElse(sys.error("No value found in " + enumeration + " for value=" + from)))
  }
}
