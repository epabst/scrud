package com.github.scrud.persistence

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.UriPath
import com.github.scrud.copy.StorageType
import com.github.scrud.platform.representation.Persistence

/**
 * Ths minimalistic Persistence support based on a UriPath.
 * It should be wrapped in an EntityPersistence or CrudPersistence when being used.
 * This is what needs to be implemented and can be mocked as needed.
 * @author Eric Pabst (epabst@gmail.com)
 */
trait ThinPersistence {
  /** Find all entity instances based on a UriPath.  Filtering may be included in the UriPath. */
  def findAll(uri: UriPath): Seq[AnyRef]

  /** Should delegate to PersistenceFactory.newWritable. */
  def newWritable(): AnyRef

  /** The type that is returned by newWritable(). */
  def writableType: StorageType = Persistence.Latest

  /** Save a created or updated entity. */
  def save(id: Option[ID], writable: AnyRef): ID

  /** Delete a set of entities by uri.
    * This should NOT delete downstream entities because that would break the existing simplistic "undo" functionality.
    * Instead, assume that the operation will handle deleting all downstream entities explicitly.
    * @return how many were deleted
    */
  def delete(uri: UriPath): Int

  /** Indicate that the persistence will no longer be used. */
  def close()
}
