package com.github.scrud.platform.node

import com.github.scrud.copy.StorageType

/**
 * A [[com.github.scrud.copy.StorageType]] for copying to and from a serialized format.
 * See [[com.github.scrud.platform.node.XmlFormat]] and [[com.github.scrud.platform.node.JsonFormat]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:16 AM
 */
trait SerializationFormat extends StorageType
