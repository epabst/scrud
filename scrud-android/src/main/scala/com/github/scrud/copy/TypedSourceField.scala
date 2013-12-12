package com.github.scrud.copy

import com.github.scrud.context.RequestContext

/**
 * A field that can be get a value out of a [[com.github.scrud.copy.Source]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:20 PM
 */
trait TypedSourceField[S <: Source,V] extends SourceField[V] {
  /** Get some value or None from the given Source. */
  def findValue(source: S, context: RequestContext): Option[V]

  /** Get some value or None from the given Source. */
  final def findValue(source: AnyRef, context: RequestContext) = findValue(source.asInstanceOf[S], context)
}
