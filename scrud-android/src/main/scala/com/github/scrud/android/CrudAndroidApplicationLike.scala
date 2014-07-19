package com.github.scrud.android

import com.github.scrud.EntityNavigation
import com.github.scrud.context.SharedContext
import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.android.backup.{DeletedEntityIdEntityType, DeletedEntityId}
import com.github.scrud.android.view.AndroidResourceAnalyzer._
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.EntityName
import scala.Some
import com.github.scrud.copy.types.MapStorage

/**
 * A scrud-enabled Android Application.
 *
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 6/26/14
 */
trait CrudAndroidApplicationLike extends SharedContext {
  def entityNavigation: EntityNavigation

  override def entityTypeMap: EntityTypeMap = entityNavigation.entityTypeMap

  private lazy val classInApplicationPackage: Class[_] = entityTypeMap.allEntityTypes.head.getClass
  lazy val rIdClasses: Seq[Class[_]] = detectRIdClasses(classInApplicationPackage)
  lazy val rLayoutClasses: Seq[Class[_]] = detectRLayoutClasses(classInApplicationPackage)

  /** Records that a deletion happened so that it is deleted from the Backup Service.
    * It's ok for this to happen immediately because if a delete is undone,
    * it will be restored independent of this support, and it will then be re-added to the Backup Service later
    * just like any new entity being added.
    */
  def recordDeletion(entityName: EntityName, id: ID, commandContext: AndroidCommandContext) {
    for (deletedEntityIdEntityType <- entityTypeMap.findEntityType(DeletedEntityId).map(_.asInstanceOf[DeletedEntityIdEntityType])) {
      val values = new MapStorage(
        deletedEntityIdEntityType.entityNameField -> Some(entityName.name),
        deletedEntityIdEntityType.entityIdField -> Some(id))
      commandContext.save(deletedEntityIdEntityType.entityName, None, MapStorage, values)
    }
  }
}
