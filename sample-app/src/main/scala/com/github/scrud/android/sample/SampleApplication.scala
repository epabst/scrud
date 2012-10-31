package com.github.scrud.android.sample

import com.github.scrud.android._
import com.github.scrud.CrudApplication
import com.github.scrud.platform.PlatformDriver


/** The sample application
  * @author Eric Pabst (epabst@gmail.com)
  */
class SampleApplication(platformDriver: PlatformDriver) extends CrudApplication(platformDriver) {
  val name = "Sample Application"

  val AuthorCrudType = new CrudType(AuthorEntityType, SQLitePersistenceFactory)
  val BookCrudType = new CrudType(BookEntityType, SQLitePersistenceFactory)
  val PublisherCrudType = new CrudType(PublisherEntityType, SQLitePersistenceFactory)

  def allCrudTypes = List(AuthorCrudType, BookCrudType, PublisherCrudType)

  def dataVersion = 2
}

class SampleAndroidApplication extends CrudAndroidApplication(new SampleApplication(AndroidPlatformDriver))

class SampleBackupAgent extends CrudBackupAgent(new SampleApplication(AndroidPlatformDriver))