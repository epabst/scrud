package com.github.scrud.platform.representation

import com.github.scrud.copy.{RepresentationByType, TargetType}
import com.github.scrud.platform.representation.FieldLevel.FieldLevel

/**
 * A TargetType for a UI type for displaying.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:16 AM
 */
case class DisplayUI(fieldLevel: FieldLevel) extends TargetType with RepresentationByType[Nothing] {
  protected def impliedTargetTypes: Seq[DisplayUI] = FieldLevel.values.toSeq.withFilter(_ < fieldLevel).map(DisplayUI(_))

  final lazy val targetTypes: Seq[DisplayUI] = this +: impliedTargetTypes
}
