package com.github.scrud.android.action

import android.content.Context
import android.app.Activity
import android.widget.Toast
import com.github.scrud.util.{Debug, Common}
import com.github.scrud.platform.{Notification, PlatformTypes}

trait AndroidNotification extends Notification {
  def context: Context

  private lazy val contextAsActivity = context.asInstanceOf[Activity]

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
    if (Debug.threading) debug("Scheduling UI thread task")
    contextAsActivity.runOnUiThread(Common.toRunnable("UI thread task", loggingDelegate)(withExceptionReporting {
      body
    }))
    if (Debug.threading) debug("Done scheduling UI thread task")
  }
}
