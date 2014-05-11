package com.github.scrud.copy

/**
 * A copy from a source using an [[com.github.scrud.copy.AdaptedField]].
 * @author epabst@gmail.com on 5/10/14.
 */
class AdaptedValue[V](targetField: TargetField[V], valueOpt: Option[V], context: CopyContext) extends BaseAdaptedValue {
  def update[T <: AnyRef](target: T): T = {
    targetField.updateValue(target, valueOpt, context)
  }
}
