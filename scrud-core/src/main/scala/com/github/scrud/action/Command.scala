package com.github.scrud.action

import com.netaporter.uri.Uri
import com.github.scrud.copy.{SourceType, StorageWithType}
import com.github.scrud.{EntityType, EntityName, EntityUriHolder}
import com.github.scrud.context.CommandContext

/**
 * An available command to perform an [[com.github.scrud.action.Action]] including parameterized state.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/19/14
 *         Time: 7:30 PM
 * @param actionKey which [[com.github.scrud.action.Action]] to invoke
 * @param uri the resource to act upon
 * @param commandHeaders any relevant headers for invoking the command or rendering its requested view
 * @param commandDataOpt data, if any, that the command will use
 */
case class Command(actionKey: ActionKey, uri: Uri, commandHeaders: Map[String,String], commandDataOpt: Option[StorageWithType])
        extends EntityUriHolder {
  def actionKeyAndEntityNameOrFail: (ActionKey, EntityName) = (actionKey, entityNameOrFail)

  /** Copy from the given source into the commandData. */
  def copyFrom(sourceType: SourceType, source: AnyRef, commandContext: CommandContext): Command = {
    val entityType: EntityType = commandContext.entityTypeMap.entityType(entityNameOrFail)
    val Some(StorageWithType(storage, storageType)) = commandDataOpt
    val updatedCommandData = entityType.copyAndUpdate(sourceType, source, storageType, storage, commandContext)
    copy(commandDataOpt = commandDataOpt.map(_.copy(storage = updatedCommandData)))
  }
}
