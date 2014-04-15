package com.github.scrud.action

import com.netaporter.uri.Uri
import com.github.scrud.{EntityName, EntityUriHolder}

/**
 * An available command to perform an [[com.github.scrud.action.Action]] including parameterized state.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/19/14
 *         Time: 7:30 PM
 * @param actionKey which [[com.github.scrud.action.Action]] to invoke
 * @param uri the resource to act upon (which may be different from the Uri used in web service requests).
 * @param commandHeaders any relevant headers for invoking the command or rendering its requested view
 */
case class Command(actionKey: ActionKey, uri: Uri, commandHeaders: Map[String,String]) extends EntityUriHolder {
  def actionKeyAndEntityNameOrFail: (ActionKey, EntityName) = (actionKey, entityNameOrFail)

  def actionDataTypeOpt: Option[ActionDataType] = actionKey.actionDataTypeOpt
}
