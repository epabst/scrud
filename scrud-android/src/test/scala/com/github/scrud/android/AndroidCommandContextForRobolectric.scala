package com.github.scrud.android

import org.robolectric.Robolectric
import com.github.scrud.android.action.AndroidNotificationForRobolectric

/**
 * An [[com.github.scrud.android.AndroidCommandContext]] for use when testing with Robolectric.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 6/28/14
 */
class AndroidCommandContextForRobolectric(application: CrudAndroidApplicationLike, activity: CrudActivity)
  extends AndroidCommandContext(activity, application) with AndroidNotificationForRobolectric {

  private def androidApplicationForRobolectric: CrudAndroidApplicationForRobolectric =
    androidApplication.asInstanceOf[CrudAndroidApplicationForRobolectric]

  private val entityTypesWithWrongPlatformDriver = entityTypeMap.allEntityTypes.filter(!_.platformDriver.isInstanceOf[AndroidPlatformDriver])
  if (!entityTypesWithWrongPlatformDriver.isEmpty) {
    sys.error("entityTypes=" + entityTypesWithWrongPlatformDriver + " were not instantiated with an AndroidPlatformDriver.  Use AndroidPlatformDriverForTesting.")
  }

  def waitUntilIdle() {
    androidApplicationForRobolectric.waitUntilIdle()
    while (Robolectric.getBackgroundScheduler.runOneTask()) {}
    androidApplicationForRobolectric.waitUntilIdle()
  }
}
