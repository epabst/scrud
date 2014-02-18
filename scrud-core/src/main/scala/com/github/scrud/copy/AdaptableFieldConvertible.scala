package com.github.scrud.copy

/**
 * Something that is convertible into an [[com.github.scrud.copy.AdaptableField]].
 * @tparam V the type of the field's value
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/17/14
 *         Time: 2:55 PM
 */
trait AdaptableFieldConvertible[V] {
  /**
   * Converts this to an [[com.github.scrud.copy.AdaptableField]].
   * @return the field
   */
  def toAdaptableField: ExtensibleAdaptableField[V]
}
