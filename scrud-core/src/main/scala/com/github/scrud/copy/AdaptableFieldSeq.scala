package com.github.scrud.copy

/**
 * A Seq of [[com.github.scrud.copy.AdaptableField]]s..
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/4/14
 *         Time: 11:52 PM
 */
abstract class AdaptableFieldSeq {
  def adaptableFields: Seq[BaseAdaptableField]

  def adapt(sourceType: SourceType, targetType: TargetType): AdaptedFieldSeq = {
    val adaptedFields = adaptableFields.flatMap(_.attemptToAdapt(sourceType, targetType))
    if (adaptedFields.isEmpty && sourceType != SourceType.none) {
      throw new UnsupportedOperationException("Either sourceType=" + sourceType + " or targetType=" + targetType + " are unknown to all of the fields in " + this)
    }
    new AdaptedFieldSeq(adaptedFields)
  }
}

object AdaptableFieldSeq {
  def apply(fields: BaseAdaptableField*): AdaptableFieldSeq = new AdaptableFieldSeq {
    override def adaptableFields: Seq[BaseAdaptableField] = fields
  }
}
