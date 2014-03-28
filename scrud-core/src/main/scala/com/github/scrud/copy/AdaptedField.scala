package com.github.scrud.copy

import com.github.scrud.context.CommandContext

/**
 * A field that has been adapted to a specific SourceType and TargetType.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/5/14
 *         Time: 12:02 AM
 */
case class AdaptedField[V](sourceField: SourceField[V], targetField: TargetField[V]) extends BaseAdaptedField {
  def copyAndUpdate[T <: AnyRef](source: AnyRef, target: T, commandContext: CommandContext) = {
    val valueOpt = sourceField.findValue(source, commandContext)
    targetField.updateValue(target, valueOpt, commandContext)
  }
}
