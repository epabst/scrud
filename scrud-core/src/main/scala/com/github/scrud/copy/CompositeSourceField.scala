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
  def findValue(source: AnyRef, context: RequestContext): Option[V] = {
    val valueOpt = sourceFields.view.flatMap { sourceField =>
      val valueOpt = sourceField.findValue(source, context)
      valueOpt
    }.headOption
    valueOpt
  }
}
