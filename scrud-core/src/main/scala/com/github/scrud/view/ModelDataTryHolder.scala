package com.github.scrud.view

import com.github.scrud.EntityUriHolder
import com.github.scrud.copy.{InstantiatingTargetType, SourceWithType}
import com.github.scrud.context.CommandContext
import scala.util.Try

/**
 * Something that holds a model data.  This provides some helpful functions.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/29/14
 *         Time: 10:34 AM
 */
trait ModelDataTryHolder extends EntityUriHolder {
  def modelDataTry: Try[SourceWithType]

  def copyAndUpdate[T <: AnyRef](targetType: InstantiatingTargetType[T], commandContext: CommandContext): Try[T] = {
    modelDataTry.map { modelData =>
      val entityType = commandContext.entityTypeMap.entityType(entityNameOrFail)
      entityType.copyAndUpdate(modelData.sourceType, modelData.source, targetType, commandContext)
    }
  }
}
