package com.github.scrud

import com.github.scrud.context.ApplicationName
import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.platform.{TestingPlatformDriver, PlatformDriver}

/**
 * An EntityNavigation for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/28/14
 *         Time: 4:29 PM
 */
class EntityNavigationForTesting(entityTypeMap: EntityTypeMap,
                                 platformDriver: PlatformDriver = TestingPlatformDriver,
                                 applicationName: ApplicationName = ApplicationNameForTesting)
    extends EntityNavigation(applicationName, entityTypeMap, platformDriver)
