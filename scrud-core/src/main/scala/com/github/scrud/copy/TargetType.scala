package com.github.scrud.copy

/**
 * A type of a target that copy can be copied to.
 * It is a key to look up the right TargetField to use.
 * It also allows filtering which fields should be copied.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:14 PM
 */
trait TargetType {
  /** Gets the FieldApplicability that is intrinsic to this Representation.  The PlatformDriver may replace this type needed. */
  def toPlatformIndependentFieldApplicability: FieldApplicability = FieldApplicability(from = Set.empty, to = Set(this))
}
