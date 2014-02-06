package com.github.scrud.persistence

import com.github.scrud._
import com.github.scrud.platform.representation.MapStorage
import com.github.scrud.context.SharedContext

class EntityPersistenceForTesting(entityType: EntityType, sharedContext: SharedContext)
    extends ListBufferCrudPersistence(new MapStorage, entityType, sharedContext, null)
