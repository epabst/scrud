package com.github.scrud.copy

/**
 * A base class for an AdaptableField which doesn't know which value type it contains.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/12/13
 *         Time: 3:08 PM
 */
abstract class BaseAdaptableField {
  def attemptToAdapt(sourceType: SourceType, targetType: TargetType): Option[BaseAdaptedField]

  def findSourceField(sourceType: SourceType): Option[SourceField[Any]]

  def findTargetField(targetType: TargetType): Option[TargetField[Nothing]]

  def hasSourceField(sourceType: SourceType) = findSourceField(sourceType).isDefined

  def hasTargetField(targetType: TargetType) = findTargetField(targetType).isDefined
}
