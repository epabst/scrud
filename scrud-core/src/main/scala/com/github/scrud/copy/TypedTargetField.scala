package com.github.scrud.copy

import com.github.scrud.context.RequestContext

/**
 * A field that can copy into a target.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:20 PM
 * @tparam D the data type
 * @tparam V the type of the field value
 */
abstract class TypedTargetField[D <: AnyRef,V] extends TargetField[V] {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  def updateFieldValue(target: D, valueOpt: Option[V], context: RequestContext): D

  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  final def updateValue[T](target: T, valueOpt: Option[V], context: RequestContext): T = {
    updateFieldValue(target.asInstanceOf[D], valueOpt, context).asInstanceOf[T]
  }
}
