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
}
