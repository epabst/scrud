package com.github.scrud.copy

/**
 * An AdaptableField based on a lookup by SourceType or TargetType.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/18/13
 *         Time: 9:07 AM
 */
class AdaptableFieldByType[V](sourceFields: Map[SourceType,SourceField[V]], targetFields: Map[TargetType,TargetField[V]])
    extends AdaptableField[V] {
  def findSourceField(sourceType: SourceType) = sourceFields.get(sourceType)

  def findTargetField(targetType: TargetType) = targetFields.get(targetType)
}
