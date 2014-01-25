package com.github.scrud.copy

import com.github.scrud.context.RequestContext

/**
 * A field that can be get a value out of a source.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:20 PM
 */
trait TypedSourceField[D <: AnyRef,V] extends SourceField[V] {
  /** Get some value or None from the given source. */
  def findFieldValue(sourceData: D, context: RequestContext): Option[V]

  /** Get some value or None from the given source. */
  final def findValue(sourceData: AnyRef, context: RequestContext) = findFieldValue(sourceData.asInstanceOf[D], context)
}

object TypedSourceField {
  def apply[D <: AnyRef,V](findValue: D => Option[V])(implicit manifest: Manifest[D]): TypedSourceField[D,V] = {
    val _findValue = findValue
    new TypedSourceField[D,V] {
      /** Get some value or None from the given source. */
      def findFieldValue(sourceData: D, context: RequestContext) = _findValue(sourceData)

      override def toString = super.toString + "[" + manifest + "]"
    }
  }
}
