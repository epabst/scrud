package com.github.scrud.android.backup

import com.github.scrud.persistence.EntityTypeMap

/**
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 7/14/14
 */
trait EntityTypeMapWithBackup extends EntityTypeMap {
  lazy val deletedEntityIdEntityType = addEntityType(new DeletedEntityIdEntityType(platformDriver), platformDriver.localDatabasePersistenceFactory)

  override def validate() {
    // Make sure that this EntityType isn't added too late.
    val entityType = deletedEntityIdEntityType
    debug("Instantiated " + entityType)
    super.validate()
  }
}
