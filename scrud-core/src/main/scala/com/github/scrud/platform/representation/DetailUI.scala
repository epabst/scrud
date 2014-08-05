package com.github.scrud.platform.representation

/**
 *  A UI Representation that shows detail information (as opposed to just summary).
 *  See [[com.github.scrud.platform.representation.SummaryUI]].
 *  @author Eric Pabst (epabst@gmail.com)
 *          Date: 12/11/13
 *          Time: 9:16 AM
 */
@deprecated("use DisplayUI(FieldLevel.Detail)", since = "2014-08-04")
object DetailUI extends DisplayUI(FieldLevel.Detail)
