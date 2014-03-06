package com.github.scrud.query

import com.github.scrud.{EntityField, EntityType, EntityName}
import com.github.scrud.platform.PlatformTypes.ID

/**
 * A single item from the result of an EntityQuery.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/27/14
 *         Time: 1:07 PM
 */
case class SelectedEntity[E <: EntityType](entityType: E, id: ID, query: EntityQuery[E]) {
  def traverse[E2 <: EntityType](field: E => EntityField[ID]): EntityQuery[E2] = {
    val entityField = field(entityType)
    val otherEntityName = entityField.qualifiedType.asInstanceOf[EntityName]
    EntityQuery[E2](otherEntityName)
  }
}
