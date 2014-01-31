package com.github.scrud.platform.representation

import com.github.scrud.copy.FieldApplicability

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
 * while the companion class is a [[com.github.scrud.platform.representation.Representation]] which has constructor parameters.
 * A PlatformDriver will convert this into a FieldApplicability.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/29/14
 *         Time: 11:20 PM
 */
trait Representation {
  /** Gets the FieldApplicability that is intrinsic to this Representation.  The PlatformDriver may replace this as needed. */
  def toPlatformIndependentFieldApplicability: FieldApplicability
}
