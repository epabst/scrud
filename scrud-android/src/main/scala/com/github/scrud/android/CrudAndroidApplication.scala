package com.github.scrud.android

import android.app.Application
import com.github.scrud.EntityNavigation
import com.github.scrud.context.SharedContext
import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.android.view.AndroidResourceAnalyzer._
import com.github.scrud.android.backup.DeletedEntityTypeMap

/**
 * A scrud-enabled Android Application.
 *
 * Because this extends android.app.Application, it can't normally be instantiated
 * except on a device.  Because of this, there is a convention
 * that each CrudApplication will have {{{class MyApplication extends CrudApplication {..} }}} that has its code,
 * then have {{{class MyAndroidApplication extends CrudAndroidApplication(new MyApplication)}}}.
 *
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/2/12
 * Time: 5:07 PM
 */
class CrudAndroidApplication(val entityNavigation: EntityNavigation) extends Application with SharedContext {
  def this(entityTypeMap: EntityTypeMap) {
    this(new EntityNavigation(entityTypeMap))
  }

  override def entityTypeMap: EntityTypeMap = entityNavigation.entityTypeMap

  private lazy val classInApplicationPackage: Class[_] = entityTypeMap.allEntityTypes.head.getClass
  lazy val rIdClasses: Seq[Class[_]] = detectRIdClasses(classInApplicationPackage)
  lazy val rLayoutClasses: Seq[Class[_]] = detectRLayoutClasses(classInApplicationPackage)

  lazy val deletedEntityTypeMap = new DeletedEntityTypeMap(applicationName, platformDriver)
}
