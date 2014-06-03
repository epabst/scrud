package com.github.scrud.context

import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.EntityType
import com.github.scrud.util.{ExternalLogging, DelegateLogging}

/**
 * Something that has a SharedContext.
 */
private[scrud] trait SharedContextHolder extends DelegateLogging {
  def sharedContext: SharedContext

  override protected def loggingDelegate: ExternalLogging = sharedContext.applicationName

  def entityTypeMap: EntityTypeMap = sharedContext.entityTypeMap

  def platformDriver: PlatformDriver = sharedContext.platformDriver

  /** Instantiates a data buffer which can be saved by EntityPersistence.
    * The EntityType must support copying into this object.
    */
  def newWritable(entityType: EntityType): AnyRef = entityTypeMap.persistenceFactory(entityType).newWritable()
}
