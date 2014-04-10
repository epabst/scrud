package com.github.scrud.view

import com.netaporter.uri.Uri
import com.github.scrud.EntityUriHolder
import com.github.scrud.action.Command

/**
 * A ViewSpecifier along with the usual commands that a user may invoke within from the view.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/29/14
 *         Time: 9:30 AM
 * @param viewSpecifier which view is requested
 * @param usualAvailableCommands the commands that a user may invoke within the view.
 */
case class ViewRequest(viewSpecifier: ViewSpecifier, usualAvailableCommands: Seq[Command]) extends EntityUriHolder {
  def uri: Uri = viewSpecifier.uri

  /** Commands that are available that the user may issue. */
  lazy val availableCommands: Seq[Command] = usualAvailableCommands ++ viewSpecifier.extraAvailableCommands
}
