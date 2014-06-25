package com.github.scrud.copy

/**
 * An AdaptableField based on a lookup by SourceType or TargetType.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/18/13
 *         Time: 9:07 AM
 */
class AdaptableFieldByType[V](private[copy] val sourceFields: Map[SourceType,SourceField[V]], private[copy] val targetFields: Map[TargetType,TargetField[V]])
    extends ExtensibleAdaptableField[V] {
  def this(sourceFields: Seq[(SourceType,SourceField[V])], targetFields: Seq[(TargetType,TargetField[V])]) {
    this(sourceFields.toMap, targetFields.toMap)
  }

  def findSourceField(sourceType: SourceType) = sourceFields.get(sourceType)

  def findTargetField(targetType: TargetType) = targetFields.get(targetType)

  override def toString: String =
    this.getClass.getSimpleName + "(sourceFields=" + sourceFields + ", targetFields=" + targetFields + ")"
}
