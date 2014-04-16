package com.github.scrud.action

import com.github.scrud.copy.StorageType

/**
 * A [[com.github.scrud.copy.StorageType]] that is used as input to performing some action.
 * It is often an appendage or filter to the real StorageType that is used for transferring the data such as
 * [[com.github.scrud.platform.representation.JsonFormat]] or [[com.github.scrud.platform.representation.XmlFormat]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/1/14
 * Time: 11:24 PM
 */
trait ActionDataType extends StorageType
