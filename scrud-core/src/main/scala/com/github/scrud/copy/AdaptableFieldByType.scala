package com.github.scrud.copy

/**
 * An AdaptableField based on a lookup by SourceType or TargetType.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/18/13
 *         Time: 9:07 AM
 */
class AdaptableFieldByType[V](private val sourceFields: Map[SourceType,SourceField[V]], private val targetFields: Map[TargetType,TargetField[V]])
    extends ExtensibleAdaptableField[V] {
  def findSourceField(sourceType: SourceType) = sourceFields.get(sourceType)

  def findTargetField(targetType: TargetType) = targetFields.get(targetType)

  // Override this to make the result much simpler to examine rather than as a composite of AdaptableFields.
  override def orElse(adaptableField: ExtensibleAdaptableField[V]) = {
    adaptableField match {
      case adaptableFieldByType: AdaptableFieldByType[V] =>
        new AdaptableFieldByType[V](
          // "++" biases the latter, but "orElse" biases the former, so the order is reversed here.
          adaptableFieldByType.sourceFields ++ sourceFields,
          adaptableFieldByType.targetFields ++ targetFields)
      case _ =>
        super.orElse(adaptableField)
    }
  }
}
