package com.github.scrud.android.view

import android.view.View
import AndroidConversions.toOnClickListener
import com.github.triangle.{Setter, UpdaterInput, &&}
import com.github.scrud.{CrudContextField, UriField}
import com.github.scrud.action.Operation

/** A Setter that invokes an Operation when the View is clicked.
  * @author Eric Pabst (epabst@gmail.com)
  */
case class OnClickOperationSetter[T](viewOperation: View => Operation)
    extends Setter[T]({
      case UpdaterInput(view: View, _, CrudContextField(Some(crudContext)) && UriField(Some(uri))) =>
        if (view.isClickable) {
          view.setOnClickListener { view: View =>
            viewOperation(view).invoke(uri, crudContext)
          }
        }
    })
