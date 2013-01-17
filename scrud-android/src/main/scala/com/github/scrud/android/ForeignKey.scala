package com.github.scrud.android

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.{EntityField, EntityType, EntityName}
import com.github.scrud.android.persistence.CursorField._

/**
 * A EntityField that is persisted..
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 6:00 PM
 */
object ForeignKey {
  def foreignKey[E <: EntityType](entityName: EntityName): EntityField[E] = {
    val entityField = EntityField[E](entityName)
    EntityField[E](entityName, entityField + persisted[ID](entityField.fieldName) + sqliteCriteria[ID](entityField.fieldName))
  }
}
