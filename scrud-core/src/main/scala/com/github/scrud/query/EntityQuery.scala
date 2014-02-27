package com.github.scrud.query

import com.github.scrud.{EntityType, EntityName}

/**
 * A query for entities of a given type.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/27/14
 *         Time: 1:07 PM
 */
case class EntityQuery[E <: EntityType](entityName: EntityName, criteria: Seq[EntityCriterion])
