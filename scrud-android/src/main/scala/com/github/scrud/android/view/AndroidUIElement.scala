package com.github.scrud.android.view

import android.view.View
import android.app.Activity

private object AndroidUIElement {
  def unapply(target: AnyRef): Option[AnyRef] = target match {
    case view: View => Some(view)
    case activity: Activity => Some(activity)
    case _ => None
  }
}
