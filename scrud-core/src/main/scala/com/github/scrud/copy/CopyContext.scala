package com.github.scrud.copy

import com.github.scrud.context._
import com.github.scrud.{EntityName, UriPath}

/**
 * A context that lasts the duration of a [[com.github.scrud.copy.AdaptedFieldSeq]].copyAndUpdate.
 * It contains a [[com.github.scrud.context.CommandContext]].
 * @author epabst@gmail.com on 5/8/14.
 */
class CopyContext(val sourceUri: UriPath, val commandContext: CommandContext) extends CommandContextDelegator {
  /** Find using this CommandContext's URI. */
  def findAll(entityName: EntityName): Seq[AnyRef] = persistenceConnection.persistenceFor(entityName).findAll(sourceUri)

  /** Find using this CommandContext's URI. */
  def findAll[T <: AnyRef](entityName: EntityName, targetType: InstantiatingTargetType[T]): Seq[T] =
    persistenceConnection.findAll(sourceUri, targetType, commandContext)
}