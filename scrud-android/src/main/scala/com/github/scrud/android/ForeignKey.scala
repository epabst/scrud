package com.github.scrud.android

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.{EntityField, EntityType, EntityName}
import com.github.scrud.android.persistence.CursorField._
import com.github.triangle.PortableField

/**
 * A EntityField that is persisted..
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 6:00 PM
 */
object ForeignKey {
  /** A persisted EntityField.
    * @param entityName the name of the Entity to persist a reference to.
    * @param fieldToGetIdElsewhere the entire PortableField for getting the ID for the entity (other than those added here).
       *              If it is incomplete, some look-ups won't happen because the ID must be gettable for a lookup to work.
       *              The default is an emptyField.
    * @tparam E the EntityType class
    * @return
    */
  def foreignKey[E <: EntityType](entityName: EntityName, fieldToGetIdElsewhere: PortableField[ID] = PortableField.emptyField): EntityField[E] = {
    val fieldName = EntityField.fieldName(entityName)
    EntityField[E](entityName, fieldToGetIdElsewhere + persisted[ID](fieldName) + sqliteCriteria[ID](fieldName))
  }
}
