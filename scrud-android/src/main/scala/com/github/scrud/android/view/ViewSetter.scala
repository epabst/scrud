package com.github.scrud.android.view

import android.view.View
import com.github.triangle.{UpdaterInput, TargetedSetter}
import com.github.scrud.android.AndroidCommandContext

/**
 * A Setter that operates on a View.  It automatically runs the operation on the UI thread.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/2/13
 *         Time: 4:01 PM
 */
case class ViewSetter[V <: View,T](body: V => Option[T] => Unit)(implicit subjectManifest: ClassManifest[V])
    extends TargetedSetter[V,T]({
      case UpdaterInput(view: V, _, CommandContextField(commandContext: AndroidCommandContext)) =>
        commandContext.runOnUiThread {
          body(view)
        }
    })

