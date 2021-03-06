package com.github.scrud.copy

import com.github.scrud.context.CommandContext

/**
 * A TargetType that knows how to instantiate a target instance.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/25/14
 *         Time: 7:51 AM
 */
trait InstantiatingTargetType[T <: AnyRef] extends TargetType {
  def makeTarget(commandContext: CommandContext): T
}
