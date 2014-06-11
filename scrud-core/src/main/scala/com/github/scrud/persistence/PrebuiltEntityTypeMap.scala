package com.github.scrud.persistence

import com.github.scrud.EntityType
import com.github.scrud.context.ApplicationName

/**
 * An EntityTypeMap that is pre-built rather than by calling addEntityType for each one.
 * Created by eric on 5/28/14.
 */
class PrebuiltEntityTypeMap(applicationName: ApplicationName, _entityTypesAndFactories: (EntityType, PersistenceFactory)*)
  extends EntityTypeMap(applicationName, _entityTypesAndFactories.head._1.platformDriver) {

  override lazy val entityTypesAndFactories: Seq[(EntityType, PersistenceFactory)] = _entityTypesAndFactories

  validate()
}
