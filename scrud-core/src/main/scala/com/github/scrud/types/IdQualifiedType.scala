package com.github.scrud.types

import com.github.scrud.platform.PlatformTypes.ID

/**
 * A QualifiedType for the main ID field of an entity.
 * It's value should be generated upon persisting it, if persisted at all.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/22/13
 * Time: 4:47 PM
 */
object IdQualifiedType extends QualifiedType[ID]
