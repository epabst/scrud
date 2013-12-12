package com.github.scrud.copy

/**
 * A set of [[com.github.scrud.copy.TargetField]]s by [[com.github.scrud.copy.TargetType]]
 * and [[com.github.scrud.copy.TypedSourceField]]s by [[com.github.scrud.copy.SourceType]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:36 AM
 */
trait AdaptableField[V] {
  def findSourceField(sourceType: SourceType): Option[SourceField[V]]

  def findTargetField(targetType: TargetType): Option[TargetField[V]]
}

object AdaptableField {
  private val Empty = new AdaptableField[Any] {
    def findSourceField(sourceType: SourceType): Option[Nothing] = None

    def findTargetField(targetType: TargetType): Option[Nothing] = None
  }

  def empty[V] = Empty.asInstanceOf[AdaptableField[V]]
}
