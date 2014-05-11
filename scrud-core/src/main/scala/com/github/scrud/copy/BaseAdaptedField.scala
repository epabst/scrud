package com.github.scrud.copy

/**
 * A base class for an AdaptedField which doesn't know which value type it contains.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/12/13
 *         Time: 3:08 PM
 */
abstract class BaseAdaptedField {
  def sourceField: SourceField[Any]

  def targetField: TargetField[Nothing]

  def copyAndUpdate[T <: AnyRef](source: AnyRef, target: T, context: CopyContext): T

  def copy(source: AnyRef, context: CopyContext): BaseAdaptedValue
}
