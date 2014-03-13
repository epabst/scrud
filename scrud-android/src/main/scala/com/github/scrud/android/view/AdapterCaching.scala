package com.github.scrud.android.view

import com.github.triangle.{UpdaterInput, Logging}
import com.github.scrud.{CrudContext, RequestContext, UriPath, EntityType}
import android.view.View
import android.os.Bundle
import android.widget.BaseAdapter
import com.github.scrud.platform.PlatformTypes._
import scala.Some
import com.github.scrud.android.state.CachedStateListener
import com.github.scrud.util.Common

trait AdapterCaching extends Logging { self: BaseAdapter =>
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

  protected[scrud] def bindViewFromCacheOrItems(view: View, position: Int, requestContext: RequestContext) {
    bindViewFromCacheOrItems(view, position, getItem(position), requestContext)
  }

  protected[scrud] def bindViewFromCacheOrItems(view: View, position: Int, entityData: AnyRef, requestContext: RequestContext) {
    bindViewFromCacheOrItems(view, entityData, requestContext.copy(currentUriPath = baseUriPath / getItemId(entityData, position)))
  }

  protected[scrud] def bindViewFromCacheOrItems(view: View, entityData: AnyRef, requestContext: RequestContext) {
    val uriPath = requestContext.currentUriPath
    view.setTag(uriPath)
    val application = requestContext.crudContext.application
    val futurePortableValue = application.futurePortableValue(entityType, uriPath, entityData, requestContext.crudContext)
    val updaterInput = UpdaterInput(view, requestContext)
    if (futurePortableValue.isSet) {
      val portableValue = futurePortableValue.apply()
      debug("Copying " + portableValue + " into " + view + " immediately")
      portableValue.update(updaterInput)
    } else {
      entityType.loadingValue.update(updaterInput)
      futurePortableValue.foreach { portableValue =>
        view.post(Common.toRunnable {
          if (view.getTag == uriPath) {
            debug("Copying " + portableValue + " into " + view + " after asynchronous calculation")
            portableValue.update(updaterInput)
            view.invalidate()
          }
        })
      }
    }
  }
}

class AdapterCachingStateListener(crudContext: CrudContext) extends CachedStateListener with Logging {
  protected def logTag = crudContext.application.logTag

  def onSaveState(outState: Bundle) {
  }

  def onRestoreState(savedInstanceState: Bundle) {
  }

  def onClearState(stayActive: Boolean) {
    crudContext.application.FuturePortableValueCache.get(crudContext.stateHolder).clear()
  }
}
