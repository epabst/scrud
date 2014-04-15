package com.github.scrud.action

import com.github.scrud.context.CommandContext
import com.github.scrud.view.ViewSpecifier
import com.netaporter.uri.Uri

/**
 * An action, usually invoked by a user.
 * It should be stateless and reusable and uniquely identifiable by its ActionKey for a given Uri using
 * [[com.github.scrud.EntityNavigation.usualAvailableActions]].
 * It can use commandContext.persistenceConnection to read and write data
 * (which interacts with a local database, remote web service, etc.).
 * It returns a [[com.github.scrud.view.ViewSpecifier]] to indicate where the user should go to next.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/19/14
 *         Time: 7:30 PM
 */
abstract class Action(val actionKey: ActionKey) {
  /**
   * Invoke this Action and specify the view to render.
   * @param actionKey the key of this Action
   * @param uri the resource to act upon (which may be different from the Uri used in web service requests).
   * @param commandContext the CommandContext, which includes platform-dependent support
   * @return a ViewSpecifier
   */
  def invoke(actionKey: ActionKey, uri: Uri, commandHeaders: Map[String,String], commandContext: CommandContext): ViewSpecifier
}
