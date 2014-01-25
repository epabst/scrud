package com.github.scrud.copy

/**
 * A SourceType or TargetType.
 * This type should be used very minimally since it reduces type-safety in distinguishing between Sources and Targets.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/13/13
 *         Time: 6:05 AM
 */
trait FieldApplicabilityItem {
  def toFieldApplicability: FieldApplicability

  def +(other: FieldApplicabilityItem): FieldApplicability = toFieldApplicability + other.toFieldApplicability
}
