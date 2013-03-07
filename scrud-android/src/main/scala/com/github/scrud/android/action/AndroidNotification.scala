package com.github.scrud.android.action

import android.content.Context
import android.app.Activity
import android.widget.Toast
import com.github.scrud.util.Common
import com.github.scrud.Notification
import com.github.scrud.platform.PlatformTypes

trait AndroidNotification extends Notification {
  def context: Context

  def displayMessageToUser(message: String) {
    runOnUiThread {
      Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
  }

  /**
   * Display a message to the user temporarily.
   * @param messageKey the key of the message to display
   */
  def displayMessageToUserBriefly(messageKey: PlatformTypes.SKey) {
    runOnUiThread {
      Toast.makeText(context, messageKey, Toast.LENGTH_SHORT).show()
    }
  }

  def runOnUiThread[T](body: => T) {
    context.asInstanceOf[Activity].runOnUiThread(Common.toRunnable(withExceptionReporting(body)))
  }
}
