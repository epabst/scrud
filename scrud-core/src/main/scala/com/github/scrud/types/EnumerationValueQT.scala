package com.github.scrud.types

import com.github.scrud.QualifiedTypeProvidingFieldName

/**
 * A QualifiedType for an Enumeration value.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/22/13
 * Time: 4:47 PM
 */
case class EnumerationValueQT[E <: Enumeration#Value](enum: Enumeration)(implicit manifest: Manifest[E]) extends QualifiedTypeProvidingFieldName[E] {
  override def toFieldName = enum.toString()
}
