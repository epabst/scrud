package com.github.scrud.copy

import com.github.scrud.context.CommandContext

/**
 * A field that can copy into a target.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:20 PM
 */
abstract class TargetField[-V] {
  /**
   * Updates the <code>target</code> subject using the <code>valueOpt</code> for this field and some context.
   * @return the updated target, which should be the target itself if mutable.
   */
  //todo add targetUri
  def updateValue[T <: AnyRef](target: T, valueOpt: Option[V], context: CommandContext): T

  /**
   * Updates the <code>target</code> subject using the <code>valueOpt</code> for this field and some context.
   * NEVER REMOVE THIS because it flags situations where an Option is passed in
   * when one never should be.
   * It's signature should always match the normal updateValue method except for accepting an Option.
   * @return the updated target, which should be the target itself if mutable.
   */
  @deprecated("use targetOpt.map(updateValue(_, valueOpt, context))")
  final def updateValue[T <: AnyRef](targetOpt: Option[T], valueOpt: Option[V], context: CommandContext): Option[T] =
    targetOpt.map(updateValue(_, valueOpt, context))
}
