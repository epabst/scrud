package com.github.scrud.android.action

import scala.collection.mutable
import com.github.scrud.platform.PlatformTypes

/**
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 7/16/14
 */
trait AndroidNotificationForRobolectric extends AndroidNotification {

  val displayedMessageKeys: mutable.Buffer[PlatformTypes.SKey] = mutable.Buffer()

  override def reportError(throwable: Throwable) {
    throw throwable
  }

  /**
   * Display a message to the user temporarily.
   * @param messageKey the key of the message to display
   */
  override def displayMessageToUserBriefly(messageKey: PlatformTypes.SKey) {
    displayedMessageKeys += messageKey
    info("Displaying messageKey=" + messageKey + " briefly")
  }
}
