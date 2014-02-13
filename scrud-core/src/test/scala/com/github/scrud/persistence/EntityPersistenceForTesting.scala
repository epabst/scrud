package com.github.scrud.persistence

import com.github.scrud._
import com.github.scrud.context.SharedContext
import com.github.scrud.copy.types.MapStorage

class EntityPersistenceForTesting(entityType: EntityType, sharedContext: SharedContext)
    extends ListBufferCrudPersistence(new MapStorage, entityType, sharedContext, null)
