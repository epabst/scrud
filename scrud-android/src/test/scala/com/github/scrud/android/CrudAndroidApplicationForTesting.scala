package com.github.scrud.android

import com.github.scrud.EntityNavigation
import com.github.scrud.persistence.EntityTypeMap

/**
 * An approximation to a scrud-enabled Android Application for use when testing.
 *
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/2/12
 * Time: 5:07 PM
 */
class CrudAndroidApplicationForTesting(val entityNavigation: EntityNavigation) extends CrudAndroidApplicationLike {
  def this(entityTypeMap: EntityTypeMap) {
    this(new EntityNavigation(entityTypeMap))
  }
}
