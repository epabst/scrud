package com.github.scrud.android.persistence

import com.github.scrud.persistence._
import com.github.scrud.{CrudContext, EntityType}
import android.content.ContentValues
import com.github.scrud.android.AndroidCrudContext

/**
 * A PersistenceFactory that uses the ContentResolver.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/23/13
 * Time: 12:05 AM
 */
class ContentResolverPersistenceFactory(delegate: PersistenceFactory) extends DelegatingPersistenceFactory(delegate) with DataListenerSetValHolder { factory =>
  /** Instantiates a data buffer which can be saved by EntityPersistence.
    * The EntityType must support copying into this object.
    */
  override def newWritable() = ContentResolverPersistenceFactory.newWritable()

  override def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = {
    val contentResolver = crudContext.asInstanceOf[AndroidCrudContext].context.getContentResolver
    new ContentResolverCrudPersistence(entityType, contentResolver, crudContext.persistenceFactoryMapping,
      listenerSet(entityType, crudContext))
  }
}

object ContentResolverPersistenceFactory {
  def newWritable() = new ContentValues()
}
