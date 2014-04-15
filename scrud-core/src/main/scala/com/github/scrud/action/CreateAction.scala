package com.github.scrud.action

import com.netaporter.uri.Uri
import com.github.scrud.context.CommandContext
import com.github.scrud.UriPath

/**
 * todo A ... 
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 4/14/14
 *         Time: 6:25 PM
 */
class CreateAction extends Action(ActionKey.Create) {
  /**
   * Invoke this Action and specify the view to render.
   * @param actionKey the key of this Action
   * @param uri the resource to act upon (which may be different from the Uri used in web service requests).
   * @param commandContext the CommandContext, which includes platform-dependent support
   * @return a ViewSpecifier
   */
  def invoke(actionKey: ActionKey, uri: Uri, commandHeaders: Map[String, String], commandContext: CommandContext) = {
    val entityName = UriPath.lastEntityNameOrFail(uri)
    commandContext.save(entityName, , UriPath.findId(uri, entityName), )
  }
}
