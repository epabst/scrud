package com.github.scrud.platform.representation

/**
 *  A UI Representation that shows detail information (as opposed to just summary).
 *  See [[com.github.scrud.platform.representation.SummaryUI]].
 *  @author Eric Pabst (epabst@gmail.com)
 *          Date: 12/11/13
 *          Time: 9:16 AM
 */
object DetailUI extends DisplayUI {
  override def impliedTargetTypes: Seq[DisplayUI] = Seq.empty
}
