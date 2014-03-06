package com.github.scrud.query

import com.github.scrud.{EntityField, EntityType, EntityName}
import com.github.scrud.platform.PlatformTypes.ID

/**
 * A query for entities of a given type.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/27/14
 *         Time: 1:07 PM
 */
case class EntityQuery[E <: EntityType](entityName: EntityName, criteria: Seq[EntityCriterion] = Nil) {
  def selectID(id: ID): SelectedEntity[E] = SelectedEntity(id, this)
}
