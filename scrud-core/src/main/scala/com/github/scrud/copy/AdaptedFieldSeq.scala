package com.github.scrud.copy

import com.github.scrud.context.RequestContext

/**
 * A Seq of [[com.github.scrud.copy.AdaptedField]]s.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/4/14
 *         Time: 11:54 PM
 */
case class AdaptedFieldSeq(adaptedFields: Seq[BaseAdaptedField]) {
  def copyAndUpdate[T <: AnyRef](source: AnyRef, target: T, requestContext: RequestContext): T = {
    var result: T = target
    for {
      adaptedField <- adaptedFields
    } result = adaptedField.copyAndUpdate(source, result, requestContext)
    result
  }
}