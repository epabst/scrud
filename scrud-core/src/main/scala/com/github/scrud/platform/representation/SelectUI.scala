package com.github.scrud.platform.representation

import com.github.scrud.copy.StorageType

/**
 * A TargetType for the UI for identifying an entity instance such as when selecting or showing a reference to it.
 * See [[com.github.scrud.platform.representation.DetailUI]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:16 AM
 */
//todo rename to IdentifyUI
object SelectUI extends DisplayUI with StorageType
