package com.github.scrud.copy

/**
 * An [[com.github.scrud.copy.AdaptableField]] built out of any number of others,
 * where each AdaptableField's SourceField is attempted until a non-empty value is found, if any,
 * and the first found TargetField is used.
 * This is final so that it can safely be unwrapped without losing anything (such as in the orElse method).
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/8/14
 *         Time: 11:08 PM
 */
final case class CompositeAdaptableField[V](delegates: Seq[ExtensibleAdaptableField[V]]) extends ExtensibleAdaptableField[V] {
  def findSourceField(sourceType: SourceType) = {
    val sourceFields = delegates.flatMap(_.findSourceField(sourceType))
    if (sourceFields.isEmpty || sourceFields.tail.isEmpty) {
      sourceFields.headOption
    } else {
      Some(CompositeSourceField[V](sourceFields))
    }
  }

  def findTargetField(targetType: TargetType) = {
    (for {
      delegate <- delegates.view
      targetField <- delegate.findTargetField(targetType)
    } yield targetField).headOption
  }

  override def orElse(adaptableField: ExtensibleAdaptableField[V]) = {
    adaptableField match {
      case composite: CompositeAdaptableField[V] =>
        //unwrap the composite to simplify debugging
        CompositeAdaptableField(delegates.toVector ++ composite.delegates)
      case _ =>
        CompositeAdaptableField(delegates.toVector :+ adaptableField)
    }
  }
}
