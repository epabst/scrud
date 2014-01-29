package com.github.scrud.context

import com.github.scrud.action.CrudOperationType
import com.github.scrud.{ApplicationNameForTesting, EntityNavigation, UriPath}
import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.platform.{TestingPlatformDriver, PlatformDriver}

/**
 * A RequestContext to use during testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/28/14
 *         Time: 4:18 PM
 */
class RequestContextForTesting(operationType: CrudOperationType.Value, uri: UriPath, sharedContext: SharedContext,
                               entityNavigation: EntityNavigation)
    extends SimpleRequestContext(operationType, uri, sharedContext, entityNavigation) {
  def this(entityTypeMap: EntityTypeMap, platformDriver: PlatformDriver) {
    this(CrudOperationType.Read, UriPath.EMPTY, new SimpleSharedContext(entityTypeMap, platformDriver),
      new EntityNavigation(ApplicationNameForTesting, entityTypeMap, platformDriver))
  }

  def this(entityTypeMap: EntityTypeMap) {
    this(entityTypeMap, TestingPlatformDriver)
  }
}
