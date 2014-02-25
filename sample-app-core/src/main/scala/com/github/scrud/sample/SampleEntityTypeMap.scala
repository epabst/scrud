package com.github.scrud.sample

import com.github.scrud.platform.PlatformDriver
import com.github.scrud.persistence.EntityTypeMap

class SampleEntityTypeMap(platformDriver: PlatformDriver) extends EntityTypeMap(platformDriver) {
  val authorEntityType = entityType(new AuthorEntityType(platformDriver), platformDriver.localDatabasePersistenceFactory)

  val bookEntityType = entityType(new BookEntityType(platformDriver), platformDriver.localDatabasePersistenceFactory)

  entityType(new PublisherEntityType(platformDriver), platformDriver.localDatabasePersistenceFactory)
}
