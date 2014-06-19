package com.github.scrud.android.persistence

import com.github.scrud.persistence._
import com.github.scrud.{EntityName, UriPath, EntityType}
import com.github.scrud.android.view.AndroidConversions._
import android.content.ContentValues
import com.github.scrud.android.AndroidCommandContext
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
class ContentResolverPersistenceFactory(delegate: PersistenceFactory)
    extends DelegatingPersistenceFactory(delegate) with DataListenerSetValHolder { factory =>
  /** Instantiates a data buffer which can be saved by EntityPersistence.
    * The EntityType must support copying into this object.
    */
  override def newWritable() = ContentResolverPersistenceFactory.newWritable()

  override def createEntityPersistence(entityType: EntityType, persistenceConnection: PersistenceConnection): CrudPersistence = {
    val commandContext = persistenceConnection.commandContext.asInstanceOf[AndroidCommandContext]
    val contentResolver = commandContext.context.getContentResolver
    val sharedContext = persistenceConnection.sharedContext
    val delegateListenerSet = listenerSet(entityType, sharedContext)
    val theListenerSet = new DelegatingListenerHolder[DataListener] {
      protected def listenerHolder = delegateListenerSet

      override def addListener(listener: DataListener) {
        ContentResolverObserverInitializationVal.get(sharedContext).getOrElseUpdate(entityType.entityName, {
          commandContext.runOnUiThread {
            val observer = new ContentObserver(new Handler()) {
              override def onChange(selfChange: Boolean) {
                commandContext.withExceptionReporting {
                  delegateListenerSet.listeners.foreach(_.onChanged())
                }
              }
            }
            contentResolver.registerContentObserver(toUri(UriPath(entityType.entityName), sharedContext.applicationName), true, observer)
          }
        })
        super.addListener(listener)
      }
    }
    new ContentResolverCrudPersistence(entityType, contentResolver, sharedContext.entityTypeMap,
      commandContext, theListenerSet)
  }
}

object ContentResolverObserverInitializationVal extends ApplicationConcurrentMapVal[EntityName,Unit]

object ContentResolverPersistenceFactory {
  def newWritable() = new ContentValues()
}
