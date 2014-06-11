package com.github.scrud.android.backup

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.EntityName
import com.github.scrud.copy.types.MapStorage
import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.context.ApplicationName
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.android.AndroidCommandContext

class DeletedEntityTypeMap(applicationName: ApplicationName, platformDriver: PlatformDriver)
    extends EntityTypeMap(applicationName, platformDriver) {
  val deletedEntityIdEntityType = new DeletedEntityIdEntityType(platformDriver)

  addEntityType(deletedEntityIdEntityType, platformDriver.localDatabasePersistenceFactory)

  /** Records that a deletion happened so that it is deleted from the Backup Service.
    * It's ok for this to happen immediately because if a delete is undone,
    * it will be restored independent of this support, and it will then be re-added to the Backup Service later
    * just like any new entity being added.
    */
  def recordDeletion(entityName: EntityName, id: ID, commandContext: AndroidCommandContext) {
    val agentCommandContext = commandContext.copy(entityTypeMap = this)
    val values = new MapStorage(
      deletedEntityIdEntityType.entityNameField -> Some(entityName.name),
      deletedEntityIdEntityType.entityIdField -> Some(id))
    agentCommandContext.save(deletedEntityIdEntityType.entityName, MapStorage, None, values)
  }
}
