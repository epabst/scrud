package com.github.scrud.android.action

import com.github.scrud.copy.{SourceType, Representation}
import com.github.scrud.android.view.ViewRef


/**
 * A SourceType for the response to a [[com.github.scrud.android.action.StartActivityForResultOperation]].
 * This is used by [[com.github.scrud.android.CrudActivity]]'s startActivityForResult.
 */
case class ActivityResult(viewRefRespondingTo: ViewRef) extends SourceType

object ActivityResult extends Representation[Nothing]
