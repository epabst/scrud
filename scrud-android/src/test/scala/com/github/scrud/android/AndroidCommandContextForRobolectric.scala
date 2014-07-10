package com.github.scrud.android

import scala.collection.mutable
import com.github.scrud.platform.PlatformTypes
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
import org.robolectric.Robolectric

/**
 * An [[com.github.scrud.android.AndroidCommandContext]] for use when testing with Robolectric.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 6/28/14
 */
class AndroidCommandContextForRobolectric(application: CrudAndroidApplicationLike, activity: CrudActivity)
  extends AndroidCommandContext(activity, application) {

  private def androidApplicationForRobolectric: CrudAndroidApplicationForRobolectric =
    androidApplication.asInstanceOf[CrudAndroidApplicationForRobolectric]

  val uiThreadExecutionContext: ExecutionContext = androidApplicationForRobolectric.toExecutionContext(Executors.newFixedThreadPool(1))

  private val entityTypesWithWrongPlatformDriver = entityTypeMap.allEntityTypes.filter(!_.platformDriver.isInstanceOf[AndroidPlatformDriver])
  if (!entityTypesWithWrongPlatformDriver.isEmpty) {
    sys.error("entityTypes=" + entityTypesWithWrongPlatformDriver + " were not instantiated with an AndroidPlatformDriver.  Use AndroidPlatformDriverForTesting.")
  }

  val displayedMessageKeys: mutable.Buffer[PlatformTypes.SKey] = mutable.Buffer()

  override def runOnUiThread[T](body: => T) {
    androidApplicationForRobolectric.runAndAddFuture(body)(uiThreadExecutionContext)
  }

  def waitUntilIdle() {
    androidApplicationForRobolectric.waitUntilIdle()
    while (Robolectric.getBackgroundScheduler.runOneTask()) {}
    androidApplicationForRobolectric.waitUntilIdle()
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
