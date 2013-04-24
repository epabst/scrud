package com.github.scrud.android.backup

import com.github.scrud.platform.PlatformDriver
import com.github.scrud.EntityType
import com.github.scrud.android.persistence.CursorField.persisted
import com.github.scrud.platform.PlatformTypes.ID

/**
 * An EntityType for storing which entities have been deleted.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 4/24/13
 *         Time: 3:11 PM
 */
class DeletedEntityIdEntityType(platformDriver: PlatformDriver) extends EntityType(DeletedEntityId, platformDriver) {
  val entityNameField = persisted[String]("entityName")
  val entityIdField = persisted[ID]("entityId")
  // not a val since not used enough to store
  def valueFields = List(entityNameField, entityIdField)
}
