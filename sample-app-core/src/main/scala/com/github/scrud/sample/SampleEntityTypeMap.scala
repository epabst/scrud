package com.github.scrud.sample

import com.github.scrud.platform.PlatformDriver
import com.github.scrud.persistence.EntityTypeMap

class SampleEntityTypeMap(platformDriver: PlatformDriver) extends EntityTypeMap(platformDriver) {
  val authorEntityType = addEntityType(new AuthorEntityType(platformDriver), platformDriver.localDatabasePersistenceFactory)

  val bookEntityType = addEntityType(new BookEntityType(platformDriver), platformDriver.localDatabasePersistenceFactory)

  addEntityType(new PublisherEntityType(platformDriver), platformDriver.localDatabasePersistenceFactory)
}
