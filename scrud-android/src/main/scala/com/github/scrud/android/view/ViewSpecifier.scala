package com.github.scrud.android.view

import android.view.View
import android.app.Activity
import com.github.scrud.copy._
import com.github.scrud.platform.PlatformTypes.ViewKey

/**
 * A SourceField that gets a View fom within a given parent View or Activity.
 * It can be used with [[com.github.scrud.copy.NestedSourceField]] or [[com.github.scrud.copy.NestedTargetField]].
 */
case class ViewSpecifier(viewRef: ViewRef, required: Boolean = true) extends SourceField[View] {
  /** Get some value or None from the given source. */
  override def findValue(activityOrView: AnyRef, context: CopyContext): Option[View] = {
    activityOrView match {
      case view: View =>
        findViewById(view, viewRef)
      case activity: Activity =>
        findViewKey().flatMap(id => Option(activity.findViewById(id)))
      case _ => None
    }
  }

  private def findViewById(parent: View, viewRef: ViewRef): Option[View] = {
    // uses the "Alternative to the ViewHolder" pattern: http://www.screaming-penguin.com/node/7767#comment-16978
    findViewKey().flatMap(id => Option(parent.getTag(id).asInstanceOf[View]).orElse {
      val foundViewOpt = Option(parent.findViewById(id))
      foundViewOpt.foreach(parent.setTag(id, _))
      foundViewOpt
    })
  }

  private def findViewKey(): Option[ViewKey] = {
    if (required) {
      Some(viewRef.viewKeyOrError)
    } else {
      viewRef.viewKeyOpt
    }
  }

  override def toString: String = "view=" + viewRef.toString
}
