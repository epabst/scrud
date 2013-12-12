package com.github.scrud.copy

import com.github.scrud.context.RequestContext

/**
 * A field that can copy into a [[com.github.scrud.copy.Target]] of a certain type.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:20 PM
 */
abstract class TypedTargetField[T <: Target,V] extends TargetField[V] {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  def putValue(target: T, valueOpt: Option[V], context: RequestContext)

  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  final def putValue(target: AnyRef, valueOpt: Option[V], context: RequestContext) = {
    putValue(target.asInstanceOf[T], valueOpt, context)
  }
}
