package com.github.scrud.android

import collection.mutable
import com.github.scrud.platform.PlatformTypes
import com.github.scrud.android.persistence.CrudContentProviderForRobolectric
import scala.concurrent.Future

/**
 * An [[com.github.scrud.android.AndroidCommandContext]] for use when testing with Robolectric.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 6/28/14
 */
class AndroidCommandContextForRobolectric(application: CrudAndroidApplicationLike, activity: CrudActivity)
  extends AndroidCommandContext(activity, application) {

  private val entityTypesWithWrongPlatformDriver = entityTypeMap.allEntityTypes.filter(!_.platformDriver.isInstanceOf[AndroidPlatformDriver])
  if (!entityTypesWithWrongPlatformDriver.isEmpty) {
    sys.error("entityTypes=" + entityTypesWithWrongPlatformDriver + " were not instantiated with an AndroidPlatformDriver.  Use AndroidPlatformDriverForTesting.")
  }

  val displayedMessageKeys: mutable.Buffer[PlatformTypes.SKey] = mutable.Buffer()

  new CrudContentProviderForRobolectric(application).register()

  override def future[T](body: => T) = Future.successful(body)

  override def runOnUiThread[T](body: => T) {
    Future.successful(body)
  }

  override def reportError(throwable: Throwable) {
    throw throwable
  }

  /**
   * Display a message to the user temporarily.
   * @param messageKey the key of the message to display
   */
  override def displayMessageToUserBriefly(messageKey: PlatformTypes.SKey) {
    displayedMessageKeys += messageKey
  }
}
