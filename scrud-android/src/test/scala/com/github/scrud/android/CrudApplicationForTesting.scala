package com.github.scrud.android

import com.github.scrud.platform.{PlatformDriver, TestingPlatformDriver}
import com.github.scrud.{EntityName, CrudApplication}

/**
 * A test application.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/23/13
 * Time: 10:53 AM
 */
class CrudApplicationForTesting(platformDriver: PlatformDriver, crudTypes: CrudType*) extends CrudApplication(platformDriver) {
  def this(crudTypes: CrudType*) {
    this(TestingPlatformDriver, crudTypes: _*)
  }

  val name = "test app"

  override lazy val primaryEntityType = crudTypes.head.entityType

  val allCrudTypes = crudTypes.toList

  override def entityNameLayoutPrefixFor(entityName: EntityName) = "test"
}
