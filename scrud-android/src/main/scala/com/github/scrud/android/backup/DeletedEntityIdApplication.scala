package com.github.scrud.android.backup

import com.github.scrud.android._
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.EntityName
import com.github.scrud.copy.types.MapStorage
import android.R
import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.context.ApplicationName

private object MyAndroidPlatformDriver extends AndroidPlatformDriver(classOf[R])

/** Helps prevent restoring entities that the user deleted when an onRestore operation happens.
  * It only contains the entityName and ID since it is not intended as a recycle bin,
  * but to delete data in the Backup Service.
  * This entity is in its own CrudApplication by itself, separate from any other CrudApplication.
  * It is intended to be in a separate database owned by the scrud-android framework.
  */
class DeletedEntityIdApplication(applicationName: ApplicationName, platformDriver: AndroidPlatformDriver) {
  val entityTypeMap = new DeletedEntityTypeMap(applicationName, platformDriver)
  val deletedEntityIdEntityType: DeletedEntityIdEntityType = entityTypeMap.deletedEntityIdEntityType

  /** Records that a deletion happened so that it is deleted from the Backup Service.
    * It's ok for this to happen immediately because if a delete is undone,
    * it will be restored independent of this support, and it will then be re-added to the Backup Service later
    * just like any new entity being added.
    */
  def recordDeletion(entityName: EntityName, id: ID, commandContext: AndroidCommandContext) {
    val agentCommandContext = commandContext
    val values = new MapStorage(
      deletedEntityIdEntityType.entityNameField -> Some(entityName.name),
      deletedEntityIdEntityType.entityIdField -> Some(id))
    agentCommandContext.save(deletedEntityIdEntityType.entityName, MapStorage, None, values)
  }
}

class DeletedEntityTypeMap(applicationName: ApplicationName, platformDriver: AndroidPlatformDriver)
    extends EntityTypeMap(applicationName, platformDriver) {
  val deletedEntityIdEntityType = new DeletedEntityIdEntityType(platformDriver)

  addEntityType(deletedEntityIdEntityType, platformDriver.localDatabasePersistenceFactory)
}
