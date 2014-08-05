package com.github.scrud.platform.representation

/**
 * A TargetType for the UI that shows only summary information (as opposed to full detail) such as displaying in a list.
 * See [[com.github.scrud.platform.representation.DetailUI]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:16 AM
 */
@deprecated("use DisplayUI(FieldLevel.Summary)", since = "2014-08-04")
object SummaryUI extends DisplayUI(FieldLevel.Summary)
