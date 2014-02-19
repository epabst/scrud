package com.github.scrud.context

import com.github.scrud.{EntityType, ApplicationNameForTesting, EntityNavigation, UriPath}
import com.github.scrud.persistence.{EntityTypeMapForTesting, PersistenceFactoryForTesting, EntityTypeMap}
import com.github.scrud.platform.{TestingPlatformDriver, PlatformDriver}
import com.github.scrud.action.CrudOperationType._

/**
 * A RequestContext to use during testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/28/14
 *         Time: 4:18 PM
 */
class RequestContextForTesting(operationType: CrudOperationType, uri: UriPath, sharedContext: SharedContext,
                               entityNavigation: EntityNavigation)
    extends SimpleRequestContext(operationType, uri, sharedContext, entityNavigation) {
  def this(entityTypeMap: EntityTypeMap, platformDriver: PlatformDriver) {
    this(Read, UriPath.EMPTY, new SimpleSharedContext(entityTypeMap, platformDriver),
      new EntityNavigation(ApplicationNameForTesting, entityTypeMap, platformDriver))
  }

  def this(entityTypeMap: EntityTypeMap) {
    this(entityTypeMap, TestingPlatformDriver)
  }

  def this(entityType: EntityType) {
    this(EntityTypeMapForTesting(new PersistenceFactoryForTesting(entityType)))
  }
}
