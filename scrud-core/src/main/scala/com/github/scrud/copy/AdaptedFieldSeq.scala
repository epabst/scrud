package com.github.scrud.copy

import com.github.scrud.context.CommandContext
import com.github.scrud.UriPath

/**
 * A Seq of [[com.github.scrud.copy.AdaptedField]]s.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/4/14
 *         Time: 11:54 PM
 */
case class AdaptedFieldSeq(adaptedFields: Seq[BaseAdaptedField]) {
  def copyAndUpdate[T <: AnyRef](source: AnyRef, sourceUri: UriPath, target: T, commandContext: CommandContext): T = {
    var result: T = target
    if (!adaptedFields.isEmpty) {
      val context = new CopyContext(sourceUri, commandContext)
      for {
        adaptedField <- adaptedFields
      } result = adaptedField.copyAndUpdate(source, result, context)
    }
    result
  }

  def copy(source: AnyRef, sourceUri: UriPath, commandContext: CommandContext): AdaptedValueSeq = {
    if (!adaptedFields.isEmpty) {
      val context = new CopyContext(sourceUri, commandContext)
      new AdaptedValueSeq(adaptedFields.map(_.copy(source, context)))
    } else {
      AdaptedValueSeq.empty
    }
  }
}
