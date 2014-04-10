package com.github.scrud.platform.representation

import com.github.scrud.copy.RepresentationByType
import com.github.scrud.action.ActionDataType

/**
 *  A UI Representation that allows editing (as opposed to just displaying).
 *  See [[com.github.scrud.platform.representation.DetailUI]].
 *  @author Eric Pabst (epabst@gmail.com)
 *          Date: 12/11/13
 *          Time: 9:16 AM
 */
object EditUI extends ActionDataType with RepresentationByType[Nothing]
