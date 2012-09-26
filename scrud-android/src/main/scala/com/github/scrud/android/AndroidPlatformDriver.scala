package com.github.scrud.android

import common.{Timing, PlatformDriver}
import android.widget.Toast
import android.content.Context
import android.view.View
import android.app.Activity

/**
 * A PlatformDriver for the Android platform.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/28/12
 *         Time: 10:23 AM
 */
class AndroidPlatformDriver(activityContext: Context, val logTag: String) extends PlatformDriver with Timing {
  def platformDriver = this

  def displayMessageToUser(message: String) {
    Toast.makeText(activityContext, message, Toast.LENGTH_LONG).show()
  }

  def runOnUiThread[T](view: View)(body: => T) {
    view.post(toRunnable(withExceptionReporting(body)))
  }

  def runOnUiThread[T](activity: Activity)(body: => T) {
    activity.runOnUiThread(toRunnable(withExceptionReporting(body)))
  }
}