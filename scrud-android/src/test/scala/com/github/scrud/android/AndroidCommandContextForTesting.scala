package com.github.scrud.android

import state.ActivityStateHolder
import collection.mutable
import com.github.scrud.platform.PlatformTypes
import scala.concurrent.Future
import com.github.scrud.persistence.{EntityTypeMapForTesting, EntityTypeMap}
import com.github.scrud.EntityType
import android.content.Context

/**
 * An [[com.github.scrud.android.AndroidCommandContext]] for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/9/13
 * Time: 10:34 PM
 */
class AndroidCommandContextForTesting(application: CrudAndroidApplicationLike,
                                      activity: ActivityStateHolder = new ActivityStateHolderForTesting)
    extends AndroidCommandContext(activity match {
      case context: Context => context
      case _ => null
    }, activity, application) {

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
