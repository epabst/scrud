package com.github.scrud.android

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.{EntityType, ParentField, EntityName}
import com.github.triangle.PortableField
import com.github.scrud.android.persistence.CursorField._

/**
 * A ParentField that is persisted..
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 6:00 PM
 */
object ForeignKey {
  def foreignKey(entityName: EntityName): PortableField[ID] = {
    val parentField = ParentField(entityName)
    parentField + persisted[ID](parentField.fieldName) + sqliteCriteria[ID](parentField.fieldName)
  }

  def foreignKey(entityType: EntityType): PortableField[ID] = foreignKey(entityType.entityName)
}
