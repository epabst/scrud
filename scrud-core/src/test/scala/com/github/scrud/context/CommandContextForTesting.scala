package com.github.scrud.context

import com.github.scrud.{EntityType, EntityNavigation}
import com.github.scrud.persistence.{EntityTypeMapForTesting, PersistenceFactoryForTesting, EntityTypeMap}

/**
 * A CommandContext to use during testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/28/14
 *         Time: 4:18 PM
 */
class CommandContextForTesting(sharedContext: SharedContext, entityNavigation: EntityNavigation)
    extends SimpleCommandContext(sharedContext, entityNavigation) {
  def this(entityTypeMap: EntityTypeMap) {
    this(new SimpleSharedContext(entityTypeMap), new EntityNavigation(entityTypeMap))
  }

  def this(entityType: EntityType) {
    this(EntityTypeMapForTesting(entityType -> PersistenceFactoryForTesting))
  }
}
