package com.github.scrud.android

import persistence.CrudContentProviderForTesting
import state.ActivityStateHolder
import com.xtremelabs.robolectric.shadows.ShadowContentResolver
import view.AndroidConversions._
import collection.mutable
import com.github.scrud.platform.PlatformTypes
import scala.concurrent.Future
import com.github.scrud.persistence.{EntityTypeMapForTesting, EntityTypeMap}
import com.github.scrud.EntityType

/**
 * An [[com.github.scrud.android.AndroidCommandContext]] for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/9/13
 * Time: 10:34 PM
 */
class AndroidCommandContextForTesting(application: CrudAndroidApplicationLike,
                                      activity: ActivityStateHolder = new ActivityStateHolderForTesting)
    extends AndroidCommandContext(null, activity, application) {

  def this(entityTypeMap: EntityTypeMap) {
    this(new CrudAndroidApplicationForTesting(entityTypeMap))
  }

  def this(entityType: EntityType) {
    this(new EntityTypeMapForTesting(entityType))
  }

  private val entityTypesWithWrongPlatformDriver = entityTypeMap.allEntityTypes.filter(!_.platformDriver.isInstanceOf[AndroidPlatformDriver])
  if (!entityTypesWithWrongPlatformDriver.isEmpty) {
    sys.error("entityTypes=" + entityTypesWithWrongPlatformDriver + " were not instantiated with an AndroidPlatformDriver.  Use AndroidPlatformDriverForTesting.")
  }

  val displayedMessageKeys: mutable.Buffer[PlatformTypes.SKey] = mutable.Buffer()

  try {
    val contentProvider = new CrudContentProviderForTesting(application)
    ShadowContentResolver.registerProvider(authorityFor(application.applicationName), contentProvider)
  } catch {
    case e: RuntimeException if Option(e.getMessage).exists(_.contains("Stub!")) =>
      warn("Failed to register ContentProvider: " + e)
  }

  override def future[T](body: => T) = Future.successful(body)

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
