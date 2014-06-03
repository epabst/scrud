package com.github.scrud.android

import com.github.scrud.platform.{PlatformDriver, TestingPlatformDriver}
import com.github.scrud.{EntityType, EntityName, CrudApplication}
import com.github.scrud.persistence.{EntityTypeMapForTesting, EntityTypeMap, PersistenceFactory}

/**
 * A test application.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/23/13
 * Time: 10:53 AM
 */
class CrudApplicationForTesting(platformDriver: PlatformDriver, entityTypeMap: EntityTypeMap) extends CrudApplication(platformDriver, entityTypeMap) {
  def this(persistenceFactoryByEntityType: (EntityType, PersistenceFactory)*) {
    this(TestingPlatformDriver, EntityTypeMapForTesting(persistenceFactoryByEntityType.toMap))
  }

  def this(entityTypeMap: EntityTypeMap) {
    this(TestingPlatformDriver, entityTypeMap)
  }

  def this(entityType1: EntityType, otherEntityTypes: EntityType*) {
    this(EntityTypeMapForTesting((entityType1 +: otherEntityTypes).toSet))
  }

  val name = "test app"

  override def entityNameLayoutPrefixFor(entityName: EntityName) = "test"
}
