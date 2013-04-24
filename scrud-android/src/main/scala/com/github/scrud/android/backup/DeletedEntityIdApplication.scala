package com.github.scrud.android.backup

import com.github.scrud.{EntityName, CrudApplication}
import com.github.scrud.android._
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.EntityName
import com.github.scrud.android.CrudType
import com.github.scrud.EntityName
import com.github.triangle.PortableValue

/** Helps prevent restoring entities that the user deleted when an onRestore operation happens.
  * It only contains the entityName and ID since it is not intended as a recycle bin,
  * but to delete data in the Backup Service.
  * This entity is in its own CrudApplication by itself, separate from any other CrudApplication.
  * It is intended to be in a separate database owned by the scrud-android framework.
  */
object DeletedEntityIdApplication extends CrudApplication(new AndroidPlatformDriver(classOf[res.R])) {
  val name = "scrud.android_deleted"

  val deletedEntityIdEntityType = new DeletedEntityIdEntityType(platformDriver)

  val allCrudTypes = List(CrudType(deletedEntityIdEntityType, platformDriver.localDatabasePersistenceFactory))

  /** Records that a deletion happened so that it is deleted from the Backup Service.
    * It's ok for this to happen immediately because if a delete is undone,
    * it will be restored independent of this support, and it will then be re-added to the Backup Service later
    * just like any new entity being added.
    */
  def recordDeletion(entityName: EntityName, id: ID, crudContext: AndroidCrudContext) {
    val agentCrudContext = crudContext.copy(application = this)
    val portableValue = PortableValue(
      deletedEntityIdEntityType.entityNameField -> entityName.name,
      deletedEntityIdEntityType.entityIdField -> id)
    val writable = portableValue.update(agentCrudContext.newWritable(deletedEntityIdEntityType))
    agentCrudContext.withEntityPersistence(deletedEntityIdEntityType) { _.save(None, writable) }
  }
}
