package com.github.scrud

import com.github.scrud.persistence.EntityTypeMap

/**
 * An EntityNavigation for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/28/14
 *         Time: 4:29 PM
 */
class EntityNavigationForTesting(entityTypeMap: EntityTypeMap)
    extends EntityNavigation(entityTypeMap)
