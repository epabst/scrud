package com.github.scrud.copy


/**
 * Where an AdaptableField applies.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 3:37 PM
 */
case class FieldApplicability(from: Set[SourceType], to: Set[TargetType]) {
  def contains(sourceType: SourceType): Boolean = from.contains(sourceType)

  def contains(targetType: TargetType): Boolean = to.contains(targetType)

  def +(other: FieldApplicability): FieldApplicability = new FieldApplicability(from ++ other.from, to ++ other.to)

  def +(other: SourceType): FieldApplicability = this + other.toPlatformIndependentFieldApplicability

  def +(other: TargetType): FieldApplicability = this + other.toPlatformIndependentFieldApplicability
}

object FieldApplicability {
  final val Empty: FieldApplicability = FieldApplicability(Set.empty, Set.empty)
}
