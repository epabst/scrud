package com.github.scrud.android

import action.{AndroidOperation, ActivityWithState}
import com.github.scrud.{UriPath, CrudApplication, EntityType}
import com.github.scrud.persistence.CrudPersistence

/** An operation that interacts with an entity's persistence.
  * The CrudContext is available as persistence.crudContext to implementing classes.
  * @author Eric Pabst (epabst@gmail.com)
  */
abstract class PersistenceOperation(entityType: EntityType, val application: CrudApplication) extends AndroidOperation {
  def invoke(uri: UriPath, persistence: CrudPersistence)

  def invoke(uri: UriPath, activity: ActivityWithState) {
    application.withEntityPersistence(entityType, activity) { persistence => invoke(uri, persistence) }
  }
}

