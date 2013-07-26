package com.github.scrud.android.view

import com.github.triangle.PortableField._
import android.view.View
import android.app.Activity
import com.github.triangle._
import com.github.scrud.android.action.OperationResponse

/** PortableField for a View resource within a given parent View */
class ViewIdField[T] private (val viewRef: ViewRef, childViewField: PortableField[T],
                              actionResponseGetter: PortableField[AnyRef], nestedViewGetter: PortableField[AnyRef])
    extends Field[T](new NestedField[T](actionResponseGetter, childViewField) +
        new NestedField[T](nestedViewGetter, childViewField)) with FieldWithDelegates[T] {

  def this(viewRef: ViewRef, childViewField: PortableField[T]) {
    this(viewRef, childViewField,
      Getter.single[AnyRef] {
        val givenViewId = viewRef.viewKeyOpt.getOrElse(View.NO_ID)
        PartialFunct {
          case actionResponse: OperationResponse if actionResponse.viewIdRespondingTo == givenViewId =>
            Some(actionResponse)
        }
      },
      Getter.single[AnyRef](new PartialFunct[AnyRef, Option[View]] {
        def isDefinedAt(subject: AnyRef) = attempt(subject).isDefined

        def attempt(subject: AnyRef) = subject match {
          case view: View =>
            ViewIdField.findViewById(view, viewRef).map(Some(_))
          case activity: Activity =>
            viewRef.viewKeyOpt.flatMap(id => Option(activity.findViewById(id))).map(Some(_))
          case _ => None
        }
      })
    )
  }

  private lazy val viewKeyMapField: PortableField[T] =
    viewRef.viewKeyOpt.map { key =>
      Getter.single[T]({
        case map: ViewKeyMap if map.contains(key) =>  map.apply(key).asInstanceOf[Option[T]]
      }) + Updater((m: ViewKeyMap) => (valueOpt: Option[T]) => m + (key -> valueOpt))
    }.getOrElse(emptyField)

  lazy val withViewKeyMapField: PortableField[T] = this + viewKeyMapField

  protected def delegates = Seq(nestedViewGetter, childViewField)

  override lazy val toString = "viewId(" + viewRef + ", " + childViewField + ")"
}

object ViewIdField {
  private def findViewById(parent: View, viewRef: ViewRef): Option[View] = {
    // uses the "Alternative to the ViewHolder" pattern: http://www.screaming-penguin.com/node/7767#comment-16978
    viewRef.viewKeyOpt.flatMap(id => Option(parent.getTag(id).asInstanceOf[View]).orElse {
      val foundViewOpt = Option(parent.findViewById(id))
      foundViewOpt.foreach(parent.setTag(id, _))
      foundViewOpt
    })
  }
}
