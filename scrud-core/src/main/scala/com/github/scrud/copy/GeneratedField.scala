package com.github.scrud.copy

import com.github.scrud.context.RequestContext

/**
 * An AdaptableField (and SourceField) whose value is generated.  It has no target field.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/7/14
 *         Time: 3:00 PM
 */
abstract class GeneratedField[V] extends ExtensibleAdaptableField[V] with SourceField[V] with Representation[V] {
  private val sourceFieldOpt: Option[SourceField[V]] = Some(this)

  /** Get some value or None from the given source. */
  def generateValue(context: RequestContext): Option[V]

  /** Get some value or None from the given source. */
  final def findValue(source: AnyRef, context: RequestContext) = generateValue(context)

  final def findSourceField(sourceType: SourceType) = sourceFieldOpt

  final def findTargetField(targetType: TargetType) = None
}
