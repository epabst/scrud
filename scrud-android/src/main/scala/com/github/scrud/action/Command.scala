package com.github.scrud.action

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.android.view.ViewRef
import com.github.scrud.platform.PlatformTypes

/** Represents something that a user can initiate.
  * @author Eric Pabst (epabst@gmail.com)
  * @param icon  The optional icon to display.
  * @param title  The title to display.
  * @param viewRef  The ViewKey (or equivalent) that represents the Command for the user to click on.  Optional.
  *   If the title is None, it can't be displayed in a context menu for a list item.
  *   If both title and icon are None,
  *   then it can't be displayed in the main options menu, but can still be triggered as a default.
  */
case class Command(icon: Option[ImgKey], title: Option[SKey], viewRef: Option[ViewRef] = None) {
  /** A CommandID that can be used to identify if it's the same as another in a list.
    * It uses the title or else the icon or else the hash code.
    */
  val commandId: PlatformTypes.CommandId = title.orElse(icon).getOrElse(##)
}
