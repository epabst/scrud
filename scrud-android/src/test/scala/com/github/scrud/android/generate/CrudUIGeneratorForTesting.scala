package com.github.scrud.android.generate

import scala.reflect.io.Path
import com.github.scrud.android.{CrudAndroidApplicationForRobolectric, EntityTypeForTesting}
import com.github.scrud.android.backup.CrudBackupAgent
import com.github.scrud.persistence.EntityTypeMapForTesting

/**
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 6/27/14
 */
class CrudUIGeneratorForTesting extends CrudUIGenerator(Path("target/generated").toDirectory, overwrite = false) {
  val _entityType = EntityTypeForTesting
  val entityTypeMap = new EntityTypeMapForTesting(_entityType)

  def generateLayoutsIfMissing() {
    generateLayouts(entityTypeMap, classOf[CrudAndroidApplicationForRobolectric], classOf[CrudBackupAgent])
  }
}

object CrudUIGeneratorForTesting extends CrudUIGeneratorForTesting
