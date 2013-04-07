package com.github.scrud.android.persistence

import com.github.scrud.persistence._
import com.github.scrud.{EntityName, UriPath, CrudContext, EntityType}
import com.github.scrud.android.view.AndroidConversions._
import android.content.ContentValues
import com.github.scrud.android.AndroidCrudContext
import android.database.ContentObserver
import android.os.Handler
import com.github.scrud.state.ApplicationConcurrentMapVal

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
    val theListenerSet = listenerSet(entityType, crudContext)

    ContentResolverObserverVal.get(crudContext.stateHolder).getOrElseUpdate(entityType.entityName, {
      val observer = new ContentObserver(new Handler()) {
        override def onChange(selfChange: Boolean) {
          theListenerSet.listeners.foreach(_.onChanged())
        }
      }
      contentResolver.registerContentObserver(toUri(UriPath(entityType.entityName), crudContext.persistenceFactoryMapping), true, observer)
      observer
    })
    new ContentResolverCrudPersistence(entityType, contentResolver, crudContext.persistenceFactoryMapping,
      theListenerSet)
  }
}

object ContentResolverObserverVal extends ApplicationConcurrentMapVal[EntityName,ContentObserver]

object ContentResolverPersistenceFactory {
  def newWritable() = new ContentValues()
}
