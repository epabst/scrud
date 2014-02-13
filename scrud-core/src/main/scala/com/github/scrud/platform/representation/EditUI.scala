package com.github.scrud.platform.representation

import com.github.scrud.copy.{RepresentationByType, StorageType}

/**
 *  A UI Representation that allows editing (as opposed to just displaying).
 *  See [[com.github.scrud.platform.representation.DetailUI]].
 *  @author Eric Pabst (epabst@gmail.com)
 *          Date: 12/11/13
 *          Time: 9:16 AM
 */
object EditUI extends StorageType with RepresentationByType
