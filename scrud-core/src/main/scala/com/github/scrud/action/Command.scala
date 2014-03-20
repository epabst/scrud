package com.github.scrud.action

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.platform.PlatformTypes

/** Represents something that a user can initiate.
  * @author Eric Pabst (epabst@gmail.com)
  * @param commandId a unique identifier for the Command
  * @param icon  The optional icon to display.
  * @param title  The title to display.
  *   If the title is None, it can't be displayed in a context menu for a list item.
  *   If both title and icon are None,
  *   then it can't be displayed in the main options menu, but can still be triggered as a default.
  */
case class Command(commandId: CommandKey, icon: Option[ImgKey], title: Option[SKey]) {
  /** A CommandNumber that can be used to identify if it's the same as another in a list.
    * It uses the title or else the icon or else the hash code.
    */
  val commandNumber: PlatformTypes.CommandNumber = title.orElse(icon).getOrElse(##)
}
