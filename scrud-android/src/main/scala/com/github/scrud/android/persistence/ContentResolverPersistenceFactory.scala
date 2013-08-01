package com.github.scrud.android.persistence

import com.github.scrud.persistence._
import com.github.scrud.{EntityName, UriPath, CrudContext, EntityType}
import com.github.scrud.android.view.AndroidConversions._
import android.content.ContentValues
import com.github.scrud.android.AndroidCrudContext
import android.database.ContentObserver
import android.os.Handler
import com.github.scrud.state.ApplicationConcurrentMapVal
import com.github.scrud.util.DelegatingListenerHolder

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
    val delegateListenerSet = listenerSet(entityType, crudContext)
    val theListenerSet = new DelegatingListenerHolder[DataListener] {
      protected def listenerHolder = delegateListenerSet

      override def addListener(listener: DataListener) {
        ContentResolverObserverInitializationVal.get(crudContext.stateHolder).getOrElseUpdate(entityType.entityName, {
          crudContext.asInstanceOf[AndroidCrudContext].runOnUiThread {
            val observer = new ContentObserver(new Handler()) {
              override def onChange(selfChange: Boolean) {
                crudContext.withExceptionReporting {
                  delegateListenerSet.listeners.foreach(_.onChanged())
                }
              }
            }
            contentResolver.registerContentObserver(toUri(UriPath(entityType.entityName), crudContext.persistenceFactoryMapping), true, observer)
          }
        })
        super.addListener(listener)
      }
    }
    new ContentResolverCrudPersistence(entityType, contentResolver, crudContext.persistenceFactoryMapping,
      theListenerSet)
  }
}

object ContentResolverObserverInitializationVal extends ApplicationConcurrentMapVal[EntityName,Unit]

object ContentResolverPersistenceFactory {
  def newWritable() = new ContentValues()
}
