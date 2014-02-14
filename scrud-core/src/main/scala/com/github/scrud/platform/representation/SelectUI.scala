package com.github.scrud.platform.representation

import com.github.scrud.copy.{RepresentationByType, StorageType}

/**
 * A TargetType for the UI for selecting an entity instance.
 * See [[com.github.scrud.platform.representation.DetailUI]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:16 AM
 */
object SelectUI extends StorageType with RepresentationByType[Nothing]
