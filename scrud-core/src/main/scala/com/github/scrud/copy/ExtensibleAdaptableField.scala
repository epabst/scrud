package com.github.scrud.copy

import com.github.scrud.copy.types.Default

/**
 * An [[com.github.scrud.copy.AdaptableField]] that can be combined with others.
 * Use [[com.github.scrud.copy.AdaptableField]] in situations where one should not be extended
 * (such as in EntityType.field since it is registering the field and assumes it is not being extended).
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:36 AM
 */
abstract class ExtensibleAdaptableField[V] extends AdaptableField[V] with AdaptableFieldConvertible[V] { self =>
  /**
   * Combines this field and the given field.
   * The result will look in this one, and if the SourceField returns no data or a SourceField isn't even found,
   * the given SourceField will be attempted.
   * @param adaptableField another AdaptableField to delegate to next if this one didn't work.
   * @return a CompositeAdaptableField
   */
  def orElse(adaptableField: ExtensibleAdaptableField[V]): ExtensibleAdaptableField[V] = {
    if (adaptableField == AdaptableField.empty) {
      this
    } else {
      CompositeAdaptableField[V](Vector(this, adaptableField))
    }
  }

  def orElse(value: V): ExtensibleAdaptableField[V] = orElse(Default(value))

  def orElse(valueOpt: Option[V]): ExtensibleAdaptableField[V] = orElse(Default(valueOpt))

  /**
   * Converts this [[com.github.scrud.copy.AdaptableField]].
   * @return the field
   */
  def toAdaptableField = this
}
