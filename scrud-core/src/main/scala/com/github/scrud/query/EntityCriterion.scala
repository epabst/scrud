package com.github.scrud.query

import com.github.scrud.EntityFieldPath

/**
 * Part of the criteria for an EntityQuery.
 */
trait EntityCriterion {
  def fieldPath: EntityFieldPath[V]
}
