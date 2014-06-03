package com.github.scrud.android.backup

import com.github.scrud.platform.PlatformDriver
import com.github.scrud.EntityType
import com.github.scrud.types.{IdQualifiedType, TitleQT}
import com.github.scrud.platform.representation.Persistence

/**
 * An EntityType for storing which entities have been deleted.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 4/24/13
 *         Time: 3:11 PM
 */
class DeletedEntityIdEntityType(platformDriver: PlatformDriver) extends EntityType(DeletedEntityId, platformDriver) {
  val entityNameField = field("entityName", TitleQT, Seq(Persistence(1)))
  val entityIdField = field("entityId", IdQualifiedType, Seq(Persistence(1)))
}
