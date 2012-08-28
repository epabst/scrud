package com.github.scrud.android.common

/**
 * A stub PlatformDriver for testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/28/12
 *         Time: 1:27 PM
 */
object StubPlatformDriver extends PlatformDriver {
  protected def logTag = getClass.getSimpleName

  /**
   * Display a message to the user temporarily.
   * @param message the message to display
   */
  def displayMessageToUser(message: String) {
    println("displayMessageToUser: " + message)
  }
}
