package com.github.scrud.copy

/**
 * A type of a source that copy can be copied from.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/10/13
 *         Time: 3:14 PM
 */
trait SourceType extends FieldApplicabilityItem {
  def toFieldApplicability: FieldApplicability = FieldApplicability(from = Set(this), Set.empty)
}
