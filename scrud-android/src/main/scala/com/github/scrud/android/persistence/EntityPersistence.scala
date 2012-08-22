package com.github.scrud.android.persistence

import com.github.scrud.android.common.PlatformTypes._
import com.github.scrud.android.common._
import concurrent.ops

trait DataListener {
  def onChanged(uri: UriPath)
}

trait AsyncDataListener extends DataListener {
  def onChanged(uri: UriPath) {
    ops.future {
      onChangedAsync(uri)
    }
  }

  def onChangedAsync(uri: UriPath)
}

/** Persistence support for an entity.
  * @author Eric Pabst (epabst@gmail.com)
  */

trait EntityPersistence extends Timing with ListenerSet[DataListener] {
  protected def logTag: String = Common.logTag

  def toUri(id: ID): UriPath

  /** Finds one result for a given uri.  The UriPath should uniquely identify an entity.
    * @throws IllegalStateException if more than one entity matches the UriPath.
    */
  def find(uri: UriPath): Option[AnyRef] = {
    val results = findAll(uri)
    if (!results.isEmpty && !results.tail.isEmpty) throw new IllegalStateException("multiple results for " + uri + ": " + results.mkString(", "))
    results.headOption
  }

  def findAll(uri: UriPath): Seq[AnyRef]

  /** Should delegate to PersistenceFactory.newWritable. */
  def newWritable: AnyRef

  /** Save a created or updated entity. */
  final def save(idOption: Option[ID], writable: AnyRef): ID = {
    val id = doSave(idOption, writable)
    listeners.foreach(_.onChanged(toUri(id)))
    id
  }

  def doSave(id: Option[ID], writable: AnyRef): ID

  /** Delete a set of entities by uri.
    * This should NOT delete child entities because that would make the "undo" functionality incomplete.
    * Instead, assume that the CrudType will handle deleting all child entities explicitly.
    */
  final def delete(uri: UriPath) {
    doDelete(uri)
    listeners.foreach(_.onChanged(uri))
  }

  def doDelete(uri: UriPath)

  def close()
}
