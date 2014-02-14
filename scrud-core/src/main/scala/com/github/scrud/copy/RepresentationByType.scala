package com.github.scrud.copy

/**
 * A [[com.github.scrud.copy.Representation]] which has an explicit set of SourceTypes and TargetTypes.
 * <p/>
 * It is common for a RepresentationByType sub-type to
 * also mix in [[com.github.scrud.copy.SourceType]] and/or [[com.github.scrud.copy.TargetType]].
 * These allow filtering which fields should be copied.
 * Alternatively, it is also common for a companion object to be a
 * [[com.github.scrud.copy.SourceType]] and/or a [[com.github.scrud.copy.TargetType]]
 * while the companion class is a [[com.github.scrud.copy.RepresentationByType]] which has constructor parameters.
 * A PlatformDriver will convert this into a FieldApplicability.
 * @tparam RV the type of the value, which should be compatible with [[com.github.scrud.types.QualifiedType]]'s type parameter.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/29/14
 *         Time: 11:20 PM
 */
trait RepresentationByType[+RV] extends Representation[RV] {
  /** Gets the FieldApplicability that is intrinsic to this Representation.  The PlatformDriver may replace this as needed. */
  def toPlatformIndependentFieldApplicability: FieldApplicability
}
