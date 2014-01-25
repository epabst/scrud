package com.github.scrud.persistence

import com.github.scrud._

class EntityPersistenceForTesting(entityType: EntityType)
    extends ListBufferCrudPersistence(Map.empty[String, Option[Any]], entityType, null)
