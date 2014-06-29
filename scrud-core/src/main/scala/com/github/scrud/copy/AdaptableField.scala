package com.github.scrud.copy

import com.github.scrud.context.CommandContext
import com.github.scrud.UriPath

/**
 * A set of [[com.github.scrud.copy.TargetField]]s by [[com.github.scrud.copy.TargetType]]
 * and [[com.github.scrud.copy.TypedSourceField]]s by [[com.github.scrud.copy.SourceType]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:36 AM
 */
abstract class AdaptableField[V] extends BaseAdaptableField { self =>
  def attemptToAdapt(sourceType: SourceType, targetType: TargetType): Option[AdaptedField[V]] = {
    for {
      sourceField <- findSourceField(sourceType)
      targetField <- findTargetField(targetType)
    } yield AdaptedField(sourceField, targetField)
  }

  def findSourceField(sourceType: SourceType): Option[SourceField[V]]

  /** Get the SourceField or fail. */
  override def sourceFieldOrFail(sourceType: SourceType): SourceField[V] =
    findSourceField(sourceType).getOrElse(throw new IllegalArgumentException(this + " has no SourceField for " + sourceType))

  private lazy val contextSourceField = sourceFieldOrFail(SourceType.none)

  def findFromContext(sourceUri: UriPath, commandContext: CommandContext): Option[V] =
    contextSourceField.findValue(SourceType.none, new CopyContext(sourceUri, commandContext))

  def findTargetField(targetType: TargetType): Option[TargetField[V]]
}

object AdaptableField {
  def apply[V](delegates: Seq[ExtensibleAdaptableField[V]]): ExtensibleAdaptableField[V] = {
    combineByType(flattenComposites(delegates).filter(_ != AdaptableField.empty))
  }

  def apply[V](sourceFields: Seq[(SourceType,SourceField[V])], targetFields: Seq[(TargetType,TargetField[V])]): AdaptableFieldByType[V] =
    new AdaptableFieldByType[V](sourceFields, targetFields)

  def apply[V](sourceFields: Map[SourceType,SourceField[V]], targetFields: Map[TargetType,TargetField[V]]): AdaptableFieldByType[V] =
    new AdaptableFieldByType[V](sourceFields, targetFields)

  private def flattenComposites[V](delegates: Seq[ExtensibleAdaptableField[V]]): Seq[ExtensibleAdaptableField[V]] = {
    val (composites: Seq[CompositeAdaptableField[V]], others) = delegates.partition(_.isInstanceOf[CompositeAdaptableField[V]])
    composites.flatMap(composite => flattenComposites(composite.delegates)) ++ others
  }

  private def combineByType[V](delegates: Seq[ExtensibleAdaptableField[V]]): ExtensibleAdaptableField[V] = {
    val (adaptableFieldByTypeSeq: Seq[AdaptableFieldByType[V]], otherAdaptableFields) =
      delegates.partition(_.isInstanceOf[AdaptableFieldByType[V]])
    if (adaptableFieldByTypeSeq.isEmpty) {
      if (otherAdaptableFields.isEmpty) {
        AdaptableField.empty[V]
      } else if (otherAdaptableFields.tail.isEmpty) {
        otherAdaptableFields.head
      } else {
        new CompositeAdaptableField[V](otherAdaptableFields)
      }
    } else {
      val fieldByType = new AdaptableFieldByType[V](
        adaptableFieldByTypeSeq.flatMap(_.sourceFields.toSeq),
        adaptableFieldByTypeSeq.flatMap(_.targetFields.toSeq))
      if (otherAdaptableFields.isEmpty) {
        fieldByType
      } else {
        new CompositeAdaptableField[V](fieldByType +: otherAdaptableFields)
      }
    }
  }

  private val Empty = new ExtensibleAdaptableField[Any] {
    def findSourceField(sourceType: SourceType): Option[Nothing] = None

    def findTargetField(targetType: TargetType): Option[Nothing] = None

    override def orElse(adaptableField: ExtensibleAdaptableField[Any]) = adaptableField
  }

  def empty[V] = Empty.asInstanceOf[ExtensibleAdaptableField[V]]
}
