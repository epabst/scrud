package com.github.scrud.platform

import com.github.triangle.Logging

/**
 * An API for an app to interact with the host platform such as Android.
 * It includes access to the current state and interaction with the User.
 * It does not represent all platform callbacks.
 * The model is that the custom platform is implemented for all applications by
 * delegating to the CrudApplication to make business logic decisions.
 * Then the CrudApplication can call into this PlatformDriver for any calls it needs to make.
 * If direct access to the specific host platform is needed by a specific app, cast this
 * to the appropriate subclass, ideally using a scala match expression.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/25/12
 *         Time: 9:57 PM
 */
trait PlatformDriver extends Logging {

  /**
   * Display a message to the user temporarily.
   * @param message the message to display
   */
  def displayMessageToUser(message: String)

  /**
   * Handle the exception by communicating it to the user and developers.
   */
  def reportError(throwable: Throwable) {
    logError("Error in " + Thread.currentThread() + " Thread", throwable)

    // Also let the user know something went wrong
    val message = "Error: " + throwable.getClass.getSimpleName + " " + throwable.getMessage
    displayMessageToUser(message)
  }
}
