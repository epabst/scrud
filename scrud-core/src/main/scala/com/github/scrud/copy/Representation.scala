package com.github.scrud.copy

/**
 * A representation of data such as persistence, XML, JSON, or display UI.
 * It should indicate any information needed about how to represent a field for a given source or target
 * such as a read-only flag or hidden flag.
 * <p/>
 * It is common for a Representation sub-type to
 * also mix in [[com.github.scrud.copy.SourceType]] and/or [[com.github.scrud.copy.TargetType]].
 * These allow filtering which fields should be copied.
 * Alternatively, it is also common for a companion object to be a
 * [[com.github.scrud.copy.SourceType]] and/or a [[com.github.scrud.copy.TargetType]]
 * while the companion class is a [[Representation]] which has constructor parameters.
 * A PlatformDriver will convert this into an AdaptableField.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/29/14
 *         Time: 11:20 PM
 */
trait Representation {
  def equals(that: Any): Boolean

  def hashCode(): Int
}