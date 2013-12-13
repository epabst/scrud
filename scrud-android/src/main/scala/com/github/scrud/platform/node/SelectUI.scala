package com.github.scrud.platform.node

import com.github.scrud.copy.TargetType

/**
 * A TargetType for the UI for selecting an entity instance.
 * See [[com.github.scrud.platform.node.DetailUI]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:16 AM
 */
object SelectUI extends TargetType {
  override def toFieldApplicability = super.toFieldApplicability + SummaryUI
}
