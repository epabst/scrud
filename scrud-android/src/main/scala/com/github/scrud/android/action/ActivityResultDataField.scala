package com.github.scrud.android.action

import com.github.scrud.copy.{CopyContext, TypedSourceField}
import android.content.Intent
import android.net.Uri

/**
 * A SourceField for the result of an activity.
 * Created by eric on 6/17/14.
 * @see [[com.github.scrud.android.action.ActivityResult]]
 */
object ActivityResultDataField extends TypedSourceField[Intent,Uri] {
  /** Get some value or None from the given source. */
  override def findFieldValue(sourceData: Intent, context: CopyContext): Option[Uri] = {
    Option(sourceData).map(_.getData)
  }
}
