package com.github.scrud.android.util

import android.view.View
import com.github.scrud.util.Common

/**
 * Simple utilities for Android Views.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/7/13
 * Time: 4:23 PM
 */
object ViewUtil {
  def withViewOnUIThread[V <: View](view: V)(f: V => Unit) {
    view.post(Common.toRunnable {
      f(view)
    })
  }

  def runOnUIThread(view: View)(f: => Unit) {
    view.post(Common.toRunnable {
      f
    })
  }
}
