package com.github.scrud.persistence

import com.github.scrud.UriPath
import java.io.Closeable
import java.util.concurrent.atomic.AtomicReference

/**
 * A findAll that may be executed over again (while the EntityPersistence is kept open),
 * saving as many resources as possible across executions.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/5/13
 * Time: 7:15 AM
 */
trait RefreshableFindAll extends Closeable {
  /** Update currentResults with the latest data. */
  def refresh()

  def currentResults: Seq[AnyRef]

  /** The UriPath used to create this instance. */
  def uri: UriPath

  /** Close this instance, releasing releases because it will never be executed again. */
  def close()
}

class SimpleRefreshableFindAll(val uri: UriPath, persistence: ThinPersistence) extends RefreshableFindAll {
  private val resultsReference = new AtomicReference(findAll)

  private def findAll: Seq[AnyRef] = {
    persistence.findAll(uri)
  }

  def refresh() {
    resultsReference.set(persistence.findAll(uri))
  }

  def currentResults = resultsReference.get()

  def close() {}
}
