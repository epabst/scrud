package com.github.scrud.copy

/**
 * A type of a target that copy can be copied to.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:14 PM
 */
trait TargetType extends FieldApplicabilityItem {
  def toFieldApplicability: FieldApplicability = FieldApplicability(from = Set.empty, to = Set(this))
}
