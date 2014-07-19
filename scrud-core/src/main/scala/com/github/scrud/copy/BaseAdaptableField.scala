package com.github.scrud.copy

/**
 * A base class for an AdaptableField which doesn't know which value type it contains.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/12/13
 *         Time: 3:08 PM
 */
abstract class BaseAdaptableField {
  def attemptToAdapt(sourceType: SourceType, targetType: TargetType): Option[BaseAdaptedField]

  def sourceFieldOrFail(sourceType: SourceType): SourceField[Any] =
    findSourceField(sourceType).getOrElse(sys.error(this + " has no SourceField for " + sourceType))

  def findSourceField(sourceType: SourceType): Option[SourceField[Any]]

  def targetFieldOrFail(targetType: TargetType): TargetField[Nothing] =
    findTargetField(targetType).getOrElse(sys.error(this + " has no TargetField for " + targetType))

  def findTargetField(targetType: TargetType): Option[TargetField[Nothing]]

  def hasSourceField(sourceType: SourceType) = findSourceField(sourceType).isDefined

  def hasSourceFieldUsingSource(sourceType: SourceType) = findSourceField(sourceType) != findSourceField(SourceType.none)

  def hasTargetField(targetType: TargetType) = findTargetField(targetType).isDefined
}
