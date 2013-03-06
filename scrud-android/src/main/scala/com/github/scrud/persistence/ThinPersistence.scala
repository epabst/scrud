package com.github.scrud.persistence

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.UriPath

/**
 * Ths minimalistic Persistence support based on a UriPath.
 * It should be wrapped in an EntityPersistence or CrudPersistence when being used.
 * This is what needs to be implemented and can be mocked as needed.
 * @author Eric Pabst (epabst@gmail.com)
 */
trait ThinPersistence {
  /** Find all entity instances based on a UriPath.  Filtering may be included in the UriPath. */
  def findAll(uri: UriPath): Seq[AnyRef]

  /** A findAll that can be refreshed.  It should be closed when no longer needed. */
  def refreshableFindAll(uri: UriPath): RefreshableFindAll = new SimpleRefreshableFindAll(uri, this)

  /** Should delegate to PersistenceFactory.newWritable. */
  def newWritable(): AnyRef

  /** Save a created or updated entity. */
  def save(id: Option[ID], writable: AnyRef): ID

  /** Delete a set of entities by uri.
    * This should NOT delete child entities because that would make the "undo" functionality incomplete.
    * Instead, assume that the CrudType will handle deleting all child entities explicitly.
    */
  def delete(uri: UriPath)

  /** Indicate that the persistence will no longer be used. */
  def close()
}
