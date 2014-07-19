package com.github.scrud.platform.representation

import com.github.scrud.copy.{RepresentationByType, TargetType}

/**
 * A TargetType for a UI type for displaying.
 * See [[com.github.scrud.platform.representation.DetailUI]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:16 AM
 */
abstract class DisplayUI extends TargetType with RepresentationByType[Nothing] {
  def impliedTargetTypes: Seq[DisplayUI]

  final lazy val targetTypes: Seq[DisplayUI] = this +: impliedTargetTypes
}
