package com.github.scrud.copy

import com.github.scrud.context.RequestContext

/**
 * A SourceField made of other SourceFields.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/10/14
 *         Time: 11:59 PM
 */
final case class CompositeSourceField[V](sourceFields: Seq[SourceField[V]]) extends SourceField[V] {
  /** Get some value or None from the given source. */
  def findValue(source: AnyRef, context: RequestContext): Option[V] = findValueInFields(source, context, sourceFields)

  private def findValueInFields(source: AnyRef, context: RequestContext, sourceFieldsToCheck: Seq[SourceField[V]]): Option[V] = {
    if (sourceFieldsToCheck.isEmpty) {
      None
    } else {
      val valueOpt = sourceFieldsToCheck.head.findValue(source, context)
      if (valueOpt.isDefined) {
        valueOpt
      } else {
        findValueInFields(source, context, sourceFieldsToCheck.tail)
      }
    }
  }

}
