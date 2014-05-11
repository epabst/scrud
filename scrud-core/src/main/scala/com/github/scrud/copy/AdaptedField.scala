package com.github.scrud.copy

/**
 * A field that has been adapted to a specific SourceType and TargetType.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/5/14
 *         Time: 12:02 AM
 */
case class AdaptedField[V](sourceField: SourceField[V], targetField: TargetField[V]) extends BaseAdaptedField {
  def copyAndUpdate[T <: AnyRef](source: AnyRef, target: T, context: CopyContext) = {
    val valueOpt = sourceField.findValue(source, context)
    targetField.updateValue(target, valueOpt, context)
  }

  override def copy(source: AnyRef, context: CopyContext): AdaptedValue[V] =
    new AdaptedValue[V](targetField, sourceField.findValue(source, context), context)
}
