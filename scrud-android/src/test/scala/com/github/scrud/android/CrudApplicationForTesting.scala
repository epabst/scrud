package com.github.scrud.android

import com.github.scrud.platform.{PlatformDriver, TestingPlatformDriver}
import com.github.scrud.{EntityType, EntityName, CrudApplication}
import com.github.scrud.persistence.PersistenceFactory

/**
 * A test application.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/23/13
 * Time: 10:53 AM
 */
class CrudApplicationForTesting(platformDriver: PlatformDriver, persistenceFactoryByEntityType: (EntityType, PersistenceFactory)*) extends CrudApplication(platformDriver) {
  def this(persistenceFactoryByEntityType: (EntityType, PersistenceFactory)*) {
    this(TestingPlatformDriver, persistenceFactoryByEntityType: _*)
  }

  def this(platformDriver: PlatformDriver, crudType1: CrudType, otherCrudTypes: CrudType*) {
    this(platformDriver, (CrudType.unapply(crudType1).get +: otherCrudTypes.map(CrudType.unapply(_).get)): _*)
  }

  def this(crudType1: CrudType, otherCrudTypes: CrudType*) {
    this(TestingPlatformDriver, crudType1, otherCrudTypes: _*)
  }

  val name = "test app"

  val allCrudTypes = persistenceFactoryByEntityType.map(tuple => CrudType(tuple._1, tuple._2))

  override def entityNameLayoutPrefixFor(entityName: EntityName) = "test"
}
