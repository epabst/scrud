package com.github.scrud.copy

import com.github.scrud.context.RequestContext

/**
 * An AdaptableField (and SourceField) whose value is calculated.  It has no target field.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/7/14
 *         Time: 3:00 PM
 */
abstract class Calculation[V] extends ExtensibleAdaptableField[V] with SourceField[V] with Representation[V] {
  private val sourceFieldOpt: Option[SourceField[V]] = Some(this)

  /** Get some value or None from the given source. */
  def calculate(context: RequestContext): Option[V]

  /** Get some value or None from the given source. */
  final def findValue(source: AnyRef, context: RequestContext) = calculate(context)

  final def findSourceField(sourceType: SourceType) = sourceFieldOpt

  final def findTargetField(targetType: TargetType) = None
}

object Calculation {
  /** A field Representation where the value is calculated (given a RequestContext). */
  def apply[V](f: RequestContext => Option[V]): Calculation[V] = {
    new Calculation[V] {
      /** Get some value or None from the given source. */
      override def calculate(context: RequestContext) = f(context)
    }
  }
}
