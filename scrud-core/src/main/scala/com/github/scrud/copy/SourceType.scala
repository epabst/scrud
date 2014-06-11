package com.github.scrud.copy

/**
 * A type of a source that copy can be copied from.
 * It is a key to look up the right SourceField to use.
 * It also allows filtering which fields should be copied.
 * @see EntityType.field and PlatformDriver.field
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:14 PM
 */
trait SourceType {
  def equals(that: Any): Boolean

  def hashCode(): Int

  /** Gets the FieldApplicability that is intrinsic to this Representation.  The PlatformDriver may replace this type needed. */
  def toPlatformIndependentFieldApplicability: FieldApplicability = FieldApplicability(from = Set(this), Set.empty)
}

object SourceType {
  /**
   * A SourceType when there is no source data.  It can also be given as the source itself.
   * Using [[com.github.scrud.copy.AdaptableField]].findFromContext will facilitate good performance through re-use.
   */
  val none: SourceType = new SourceType {}
}
