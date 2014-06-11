package com.github.scrud.android.view

import android.view.View
import android.os.Bundle
import android.widget.BaseAdapter
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.android.state.CachedStateListener
import com.github.scrud.util.Logging
import com.github.scrud.context.CommandContext
import com.github.scrud.{UriPath, EntityType}
import com.github.scrud.copy.{SourceType, TargetType, CopyContext}
import com.github.scrud.android.AndroidCommandContext

trait AdapterCaching extends Logging { self: BaseAdapter =>
  def entityType: EntityType

  /** The type that this Adapter holds. */
  def adapterSourceType: SourceType

  /** The type that the View represents. */
  def targetType: TargetType

  protected def logTag = entityType.logTag

  /** The UriPath that does not contain the entities. */
  protected def uriPathWithoutEntityId: UriPath

  protected lazy val baseUriPath: UriPath = uriPathWithoutEntityId.specify(entityType.entityName)

  protected def commandContext: AndroidCommandContext

  private lazy val context = new CopyContext(uriPathWithoutEntityId, commandContext)

  private lazy val idSourceFieldOpt = entityType.idField.findSourceField(adapterSourceType)

  def getItemId(item: AnyRef, position: Int): ID =
    idSourceFieldOpt.flatMap(_.findValue(item, context)).getOrElse(position)

  protected[scrud] def bindViewFromCacheOrItems(view: View, position: Int, commandContext: CommandContext) {
    bindViewFromCacheOrSource(view, position, adapterSourceType, getItem(position), commandContext)
  }

  protected[scrud] def bindViewFromCacheOrSource(view: View, position: Int, sourceType: SourceType, source: AnyRef, commandContext: CommandContext) {
    val uri = baseUriPath / getItemId(source, position)
    bindViewFromCacheOrSource(view, sourceType, source, new CopyContext(uri, commandContext))
  }

  protected[scrud] def bindViewFromCacheOrSource(view: View, sourceType: SourceType, source: AnyRef, context: CopyContext) {
    commandContext.populateFromSource(entityType, sourceType, source, context.sourceUri, targetType, view)
  }
}

class AdapterCachingStateListener(commandContext: AndroidCommandContext) extends CachedStateListener {
  def onSaveState(outState: Bundle) {
  }

  def onRestoreState(savedInstanceState: Bundle) {
  }

  def onClearState(stayActive: Boolean) {
    commandContext.androidApplication.FuturePortableValueCache.get(commandContext.stateHolder).clear()
  }
}
