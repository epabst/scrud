package com.github.scrud.android.sample

import com.github.scrud.android._
import com.github.scrud.CrudApplication
import com.github.scrud.platform.PlatformDriver


/** The sample application
  * @author Eric Pabst (epabst@gmail.com)
  */
class SampleApplication(platformDriver: PlatformDriver) extends CrudApplication(platformDriver) {
  val name = "Sample Application"

  val authorEntityType = new AuthorEntityType(platformDriver)
  val bookEntityType = new BookEntityType(platformDriver)
  val publisherEntityType = new PublisherEntityType(platformDriver)

  val allCrudTypes = List(
    new CrudType(authorEntityType, platformDriver.localDatabasePersistenceFactory),
    new CrudType(bookEntityType, platformDriver.localDatabasePersistenceFactory),
    new CrudType(publisherEntityType, platformDriver.localDatabasePersistenceFactory))

  val dataVersion = 2
}

class SampleApplicationForAndroid extends SampleApplication(new AndroidPlatformDriver(classOf[R]))

class SampleAndroidApplication extends CrudAndroidApplication(new SampleApplicationForAndroid)

class SampleBackupAgent extends CrudBackupAgent(new SampleApplicationForAndroid)
