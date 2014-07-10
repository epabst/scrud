package com.github.scrud.android

import android.app.Application
import com.github.scrud.EntityNavigation
import com.github.scrud.persistence.{DataListener, EntityTypeMap}
import com.github.scrud.android.view.AndroidConversions
import com.github.scrud.context.SharedContext

/**
 * A scrud-enabled Android Application.
 *
 * Because this extends android.app.Application, it can't normally be instantiated
 * except on a device.  Because of this, tests can use CrudAndroidApplicationLike instead.
 *
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/2/12
 * Time: 5:07 PM
 */
class CrudAndroidApplication(val entityNavigation: EntityNavigation) extends Application with CrudAndroidApplicationLike { self =>
  def this(entityTypeMap: EntityTypeMap) {
    this(new EntityNavigation(entityTypeMap))
  }

  for {
    entityType <- entityTypeMap.allEntityTypes
    persistenceFactory = entityTypeMap.persistenceFactory(entityType)
    dataListener = new DataListener {
      override def onChanged() {
        val sharedContext: SharedContext = self
        val uri = AndroidConversions.toUri(entityType.toUri, sharedContext)
        debug("Will notify ContentResolver of change to uri=" + uri)
        // This must be done in a separate thread because at least when using Robolectric,
        // when a save happens, it notifies DataListeners, which here notifies ContentResolver,
        // which loops over ContentObservers, including notifying Loaders,
        // which force a load of the data, which causes getting a new Cursor,
        // which the Loader then adds a ContentObserver to,
        // which indirectly causes a ConcurrentModificationException in the ContentResolver loop.
        future {
          info("Notifying ContentResolver of change to uri=" + uri)
          getContentResolver.notifyChange(uri, null)
          debug("Notified ContentResolver of change to uri=" + uri)
        }
      }
    }
  } persistenceFactory.addListener(dataListener, entityType, self)
}
