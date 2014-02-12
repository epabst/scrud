package com.github.scrud.copy

/**
 * Both a [[com.github.scrud.copy.TargetType]] and a [[com.github.scrud.copy.SourceType]]
 * in that copy can be copied to and from it.
 * It is simply a key to look up the right SourceField or TargetField to use.
 * @see EntityType.field and PlatformDriver.field
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/10/13
 * Time: 3:14 PM
 */
trait StorageType extends TargetType with SourceType {
  /** Gets the intrinsic FieldApplicability.  The PlatformDriver may replace this type needed. */
  override def toPlatformIndependentFieldApplicability: FieldApplicability = FieldApplicability(from = Set(this), to = Set(this))
}
