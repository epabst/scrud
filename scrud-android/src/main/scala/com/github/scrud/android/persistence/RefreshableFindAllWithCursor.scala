package com.github.scrud.android.persistence

import com.github.scrud.persistence.RefreshableFindAll
import com.github.scrud.UriPath
import java.util.concurrent.atomic.AtomicReference
import android.database.Cursor

/**
 * A RefreshableFindAll for SQLite.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/5/13
 * Time: 7:43 AM
 */
case class RefreshableFindAllWithCursor(uri: UriPath, cursor: Cursor, entityTypePersistedInfo: EntityTypePersistedInfo)
    extends RefreshableFindAll {
  private val currentCursorStreamReference = new AtomicReference[CursorStream](CursorStream(cursor, entityTypePersistedInfo))

  def refresh() {
    cursor.requery()
      // This uses the same Cursor, but clears any cached state about the Cursor.
    currentCursorStreamReference.set(currentCursorStreamReference.get().copy())
  }

  def currentResults: CursorStream = currentCursorStreamReference.get()

  /** Close this instance, releasing releases because it will never be executed again. */
  def close() {
    cursor.close()
  }
}

object RefreshableFindAllWithCursor {
  def apply(uri: UriPath, cursorStream: CursorStream): RefreshableFindAllWithCursor =
    RefreshableFindAllWithCursor(uri, cursorStream.cursor, cursorStream.entityTypePersistedInfo)
}
