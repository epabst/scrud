package com.github.scrud.context

import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.platform.PlatformDriver

/**
 * Something that has a SharedContext.
 */
private[context] trait SharedContextHolder {
  def sharedContext: SharedContext

  def entityTypeMap: EntityTypeMap = sharedContext.entityTypeMap

  def platformDriver: PlatformDriver = sharedContext.platformDriver
}
