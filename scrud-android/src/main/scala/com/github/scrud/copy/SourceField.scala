package com.github.scrud.copy

import com.github.scrud.context.RequestContext

/**
 * A field that can be get a value out of a [[com.github.scrud.copy.Source]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:20 PM
 */
trait SourceField[V] {
  /** Get some value or None from the given Source. */
  def findValue(source: AnyRef, context: RequestContext): Option[V]
}
