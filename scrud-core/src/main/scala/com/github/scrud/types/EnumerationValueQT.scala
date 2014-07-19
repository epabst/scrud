package com.github.scrud.types

import com.github.scrud.QualifiedTypeProvidingFieldName
import com.github.scrud.converter.Converter
import scala.util.Try

/**
 * A QualifiedType for an Enumeration value.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/22/13
 * Time: 4:47 PM
 */
case class EnumerationValueQT[E <: Enumeration#Value](enum: Enumeration)(implicit manifest: Manifest[E]) extends QualifiedTypeProvidingFieldName[E] with StringConvertibleQT[E] {
  override def toFieldName: String = enum.toString()

  /** Convert the value to a String for display. */
  def convertToDisplayString(value: E) = convertToString(value)

  /** Convert the value to a String for editing.  */
  def convertToString(value: E) = value.toString

  /** Convert the value from a String (whether for editing or display. */
  def convertFromString(string: String): Try[E] = Converter.stringToEnum(enum).convert(string)
}
