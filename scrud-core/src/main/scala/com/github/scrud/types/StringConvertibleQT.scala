package com.github.scrud.types

import scala.util.Try

/**
 * A [[com.github.scrud.types.QualifiedType]] that can be converted to/from a String.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/31/14
 *         Time: 3:18 PM
 */
trait StringConvertibleQT[V] extends QualifiedType[V] {
  def convertToString(value: V): String
  def convertFromString(string: String): Try[V]
}
