package com.github.scrud

import com.github.scrud.platform.PlatformDriver
import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.context.ApplicationName
import com.github.scrud.action.{StartEntityDeleteOperation, Action}

/**
 * An application that uses scrud.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/25/14
 *         Time: 9:01 AM
 */
class EntityNavigation(val applicationName: ApplicationName, val entityTypeMap: EntityTypeMap, val platformDriver: PlatformDriver) {
  def actionToDelete(entityName: EntityName): Option[Action] = actionToDelete(entityTypeMap.entityType(entityName))

  def actionToDelete(entityType: EntityType): Option[Action] = {
    if (entityTypeMap.isDeletable(entityType)) {
      Some(Action(platformDriver.commandToDeleteItem(entityType.entityName), StartEntityDeleteOperation(entityType)))
    } else None
  }
}
