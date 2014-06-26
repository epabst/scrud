package com.github.scrud.copy

/**
 * A TargetField that has a TargetField that operates on only part of the target.
 * @author epabst@gmail.com on 5/15/14.
 */
class NestedTargetField[V](partSpecifier: SourceField[AnyRef], val nestedField: TargetField[V]) extends TargetField[V] {
  /**
   * Updates the <code>target</code> subject using the <code>valueOpt</code> for this field and some context.
   * @return the updated target, which should be the target itself if mutable.
   */
  override def updateValue[T <: AnyRef](target: T, valueOpt: Option[V], context: CopyContext): T = {
    val nestedTargetOpt = partSpecifier.findValue(target, context)
    nestedTargetOpt.fold(target)(nestedField.updateValue(_, valueOpt, context).asInstanceOf[T])
  }

  override lazy val toString = "NestedTargetField(" + partSpecifier + ", " + nestedField + ")"
}
