package com.github.scrud.android.view

import com.github.triangle.{UpdaterInput, GetterInput, Logging}
import com.github.scrud.{UriPath, EntityType}
import android.view.View
import android.os.Bundle
import android.widget.{Adapter, AdapterView, BaseAdapter}
import com.github.scrud.platform.PlatformTypes._
import scala.Some
import com.github.scrud.android.{AndroidPlatformDriver, AndroidCrudContext}
import com.github.scrud.android.state.CachedStateListener

trait AdapterCaching extends Logging { self: BaseAdapter =>
  final def platformDriver = AndroidPlatformDriver

  def entityType: EntityType

  protected def logTag = entityType.logTag

  /** The UriPath that does not contain the entities. */
  protected def uriPathWithoutEntityId: UriPath

  protected lazy val baseUriPath: UriPath = uriPathWithoutEntityId.specify(entityType.entityName)

  lazy val IdField = entityType.IdField

  def getItemId(item: AnyRef, position: Int): ID = item match {
    case IdField(Some(id)) => id
    case _ => position
  }

  protected[scrud] def bindViewFromCacheOrItems(view: View, position: Int, crudContext: AndroidCrudContext, contextItems: GetterInput) {
    bindViewFromCacheOrItems(view, position, getItem(position), crudContext, contextItems)
  }

  protected[scrud] def bindViewFromCacheOrItems(view: View, position: Int, entityData: AnyRef, crudContext: AndroidCrudContext, contextItems: GetterInput) {
    bindViewFromCacheOrItems(view, entityData, crudContext, contextItems, baseUriPath / getItemId(entityData, position))
  }

  protected[scrud] def bindViewFromCacheOrItems(view: View, entityData: AnyRef, crudContext: AndroidCrudContext, contextItems: GetterInput, uriPath: UriPath) {
    view.setTag(uriPath)
    val application = crudContext.application
    val futurePortableValue = application.futurePortableValue(entityType, uriPath, entityData, crudContext)
    val updaterInput = UpdaterInput(view, contextItems)
    if (futurePortableValue.isSet) {
      futurePortableValue().update(updaterInput)
    } else {
      entityType.loadingValue.update(updaterInput)
      futurePortableValue.foreach { portableValue =>
        crudContext.runOnUiThread {
          if (view.getTag == uriPath) {
            portableValue.update(updaterInput)
          }
        }
      }
    }
  }
}

class AdapterCachingStateListener[A <: Adapter](adapterView: AdapterView[A], entityType: EntityType, crudContext: AndroidCrudContext, adapterFactory: => A) extends CachedStateListener with Logging {
  protected def logTag = entityType.logTag

  def onSaveState(outState: Bundle) {
  }

  def onRestoreState(savedInstanceState: Bundle) {
  }

  def onClearState(stayActive: Boolean) {
    crudContext.application.FuturePortableValueCache.get(crudContext).clear()
    if (stayActive) {
      adapterView.setAdapter(adapterFactory)
    }
  }
}
