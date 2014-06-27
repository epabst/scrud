package com.github.scrud.android

import android.app.Application
import com.github.scrud.EntityNavigation
import com.github.scrud.persistence.EntityTypeMap

/**
 * A scrud-enabled Android Application.
 *
 * Because this extends android.app.Application, it can't normally be instantiated
 * except on a device.  Because of this, tests can use CrudAndroidApplicationLike instead.
 *
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/2/12
 * Time: 5:07 PM
 */
class CrudAndroidApplication(val entityNavigation: EntityNavigation) extends Application with CrudAndroidApplicationLike {
  def this(entityTypeMap: EntityTypeMap) {
    this(new EntityNavigation(entityTypeMap))
  }
}
