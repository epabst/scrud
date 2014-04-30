package com.github.scrud.types

import com.github.scrud.platform.PlatformTypes.ID
import scala.util.Try

/**
 * A QualifiedType for the main ID field of an entity.
 * It's value should be generated upon persisting it, if persisted at all.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/22/13
 * Time: 4:47 PM
 */
object IdQualifiedType extends StringConvertibleQT[ID] {
  /** Convert the value to a String for display. */
  def convertToString(value: ID) = value.toString

  /** Convert the value to a String for editing.  This may simply call convertToString(value). */
  def convertToEditString(value: ID) = convertToString(value)

  /** Convert the value from a String (whether for editing or display. */
  def convertFromString(string: String) = Try(string.toLong)
}
