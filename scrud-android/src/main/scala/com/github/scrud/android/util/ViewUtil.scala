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
  /** Operate on a View in the UI Thread.  Note: this does not do any error reporting. */
  def withViewOnUIThread[V <: View](view: V)(f: V => Unit) {
    view.post(Common.toRunnable {
      f(view)
    })
  }
}
