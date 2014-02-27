package com.github.scrud

import com.github.scrud.platform.PlatformTypes.ID

/**
 * A path to a field value.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/27/14
 *         Time: 3:20 PM
 */
case class EntityFieldPath[V](pathToEntityWithField: Seq[EntityField[ID]], field: EntityField[V])
