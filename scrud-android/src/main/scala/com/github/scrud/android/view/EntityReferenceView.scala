package com.github.scrud.android.view

import com.github.scrud.platform.PlatformTypes.ID
import com.github.scrud.{UriPath, EntityName}
import android.view.View
import com.github.scrud.android.AndroidCommandContext
import com.github.scrud.copy.CopyContext
import com.github.scrud.platform.representation.SummaryUI

/**
 * A [[com.github.scrud.copy.TargetField]] for showing a reference to an entity.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 5/2/14
 *         Time: 9:23 PM
 */
case class EntityReferenceView(entityName: EntityName)
    extends TypedViewTargetField[View,ID](<LinearLayout android:layout_width="wrap_content" android:layout_height="wrap_content" android:orientation="horizontal">
    <content/></LinearLayout>) {

  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  def updateFieldValue(view: View, idOpt: Option[ID], commandContext: AndroidCommandContext, context: CopyContext) = {
    val uri = UriPath.specify(context.sourceUri, entityName, idOpt)
    context.findOrElseDefault(uri, SummaryUI, view)
  }
}
