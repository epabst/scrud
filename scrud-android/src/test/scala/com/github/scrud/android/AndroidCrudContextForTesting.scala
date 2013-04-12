package com.github.scrud.android

import com.github.scrud.util.ReadyFuture
import com.github.scrud.CrudApplication
import android.content.Context
import persistence.CrudContentProviderForTesting
import state.ActivityStateHolder
import com.xtremelabs.robolectric.shadows.ShadowContentResolver
import view.AndroidConversions._
import collection.mutable
import com.github.scrud.platform.PlatformTypes

/**
 * An [[com.github.scrud.android.AndroidCrudContext]] for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/9/13
 * Time: 10:34 PM
 */
class AndroidCrudContextForTesting(application: CrudApplication,
                                   activity: Context with ActivityStateHolder = new ActivityStateHolderForTesting)
    extends AndroidCrudContext(activity, application) {

  val displayedMessageKeys: mutable.Buffer[PlatformTypes.SKey] = mutable.Buffer()

  val contentProvider = new CrudContentProviderForTesting(application)
  ShadowContentResolver.registerProvider(authorityFor(application.packageName), contentProvider)

  override def future[T](body: => T) = new ReadyFuture[T](body)

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
