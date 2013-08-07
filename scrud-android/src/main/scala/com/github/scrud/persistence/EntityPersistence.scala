package com.github.scrud.persistence

import com.github.scrud.util.{Common, ListenerSet}
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.UriPath

/** Persistence support for an entity.
  * @author Eric Pabst (epabst@gmail.com)
  */

trait EntityPersistence extends ThinPersistence with ListenerSet[DataListener] {
  protected def logTag: String = Common.logTag

  def toUri(id: ID): UriPath

  /** Finds one result for a given uri.  The UriPath should uniquely identify an entity.
    * @throws IllegalStateException if more than one entity matches the UriPath.
    */
  def find(uri: UriPath): Option[AnyRef] = {
    val results = findAll(uri)
    if (!results.isEmpty && !results.tail.isEmpty) {
      throw new IllegalStateException("multiple results for " + uri + ": " + results.mkString(", ") + " in " + this)
    }
    results.headOption
  }

  def findAll(uri: UriPath): Seq[AnyRef]

  /** Should delegate to PersistenceFactory.newWritable. */
  def newWritable(): AnyRef

  /** Save a created or updated entity. */
  final def save(idOption: Option[ID], writable: AnyRef): ID = {
    val id = doSave(idOption, writable)
    listeners.foreach(_.onChanged())
    id
  }

  def doSave(id: Option[ID], writable: AnyRef): ID

  /** Delete a set of entities by uri.
    * This should NOT delete child entities because that would make the "undo" functionality incomplete.
    * Instead, assume that the CrudType will handle deleting all child entities explicitly.
    * @return how many were deleted
    */
  final def delete(uri: UriPath): Int = {
    val result = doDelete(uri)
    listeners.foreach(_.onChanged())
    result
  }

  /** @return how many were deleted */
  def doDelete(uri: UriPath): Int

  def close()
}
