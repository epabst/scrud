package com.github.scrud.android

import com.github.scrud.persistence.PersistenceFactory
import com.github.scrud.EntityType

/** An entity configuration that provides all custom information needed to
  * implement CRUD on the entity.  This shouldn't depend on the platform (e.g. android).
  * @author Eric Pabst (epabst@gmail.com)
  */
case class CrudType(entityType: EntityType, persistenceFactory: PersistenceFactory)
