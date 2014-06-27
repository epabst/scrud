package com.github.scrud.android

import com.github.scrud.EntityNavigation
import com.github.scrud.context.SharedContext
import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.android.backup.DeletedEntityTypeMap
import com.github.scrud.android.view.AndroidResourceAnalyzer._

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

  lazy val deletedEntityTypeMap = new DeletedEntityTypeMap(applicationName, platformDriver)
}
