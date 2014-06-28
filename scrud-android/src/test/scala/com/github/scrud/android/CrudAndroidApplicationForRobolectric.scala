package com.github.scrud.android

import com.github.scrud.persistence.EntityTypeMapForTesting
import com.github.scrud.EntityNavigation

/**
 * An approximation to a scrud-enabled Android Application for use when testing.
 *
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/2/12
 * Time: 5:07 PM
 */
class CrudAndroidApplicationForRobolectric(entityNavigation: EntityNavigation) extends CrudAndroidApplication(entityNavigation) {
  def this() {
    this(new EntityNavigation(new EntityTypeMapForTesting(EntityTypeForTesting)))
  }
}
