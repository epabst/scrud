package com.github.scrud.context

import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.platform.{TestingPlatformDriver, PlatformDriver}

/**
  * A SharedContext to use during testing.
  * @author Eric Pabst (epabst@gmail.com)
  *         Date: 1/28/14
  *         Time: 4:18 PM
  */
class SharedContextForTesting(entityTypeMap: EntityTypeMap, platformDriver: PlatformDriver = TestingPlatformDriver)
    extends SimpleSharedContext(entityTypeMap, platformDriver)
