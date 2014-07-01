package com.github.scrud.android.persistence

import android.database.Cursor
import com.github.scrud.EntityType
import com.github.scrud.platform.representation.Persistence
import com.github.scrud.copy.types.MapStorage
import com.github.scrud.context.CommandContext

case class EntityTypePersistedInfo(entityType: EntityType) {
  lazy val currentPersistedFields = entityType.currentPersistedFields
  lazy val currentPersistedFieldNames = entityType.currentPersistedFields.
    map(field => AndroidContentAdaptableFieldFactory.toPersistedFieldName(field.fieldName)).toIndexedSeq
  lazy val queryFieldNames: Seq[String] = currentPersistedFieldNames

  /** Copies the current row of the given cursor to a Map.  This allows the Cursor to then move to a different position right after this. */
  def copyRowToMap(cursor: Cursor, commandContext: CommandContext): MapStorage =
    entityType.copyAndUpdate(Persistence.Latest, cursor, entityType.toUri, MapStorage, commandContext)
}

/** A Stream that wraps a Cursor.
  * @author Eric Pabst (epabst@gmail.com)
  */
case class CursorStream(cursor: Cursor, entityTypePersistedInfo: EntityTypePersistedInfo, commandContext: CommandContext) extends Stream[MapStorage] {
  override lazy val headOption: Option[MapStorage] = {
    if (cursor.moveToNext) {
      val entityType = entityTypePersistedInfo.entityType
      val storage = entityType.copyAndUpdate(Persistence.Latest, cursor, entityType.toUri, CursorStream.storageType, commandContext)
      Some(storage)
    } else {
      cursor.close()
      None
    }
  }

  override def isEmpty : scala.Boolean = headOption.isEmpty
  override def head = headOption.get
  // this assumes that the Cursor should be treated as immutable
  override lazy val length = cursor.getCount

  def tailDefined = !isEmpty
  // Must be a val so that we don't create more than one CursorStream.
  // Must be lazy so that we don't instantiate the entire stream
  override lazy val tail = if (tailDefined) CursorStream(cursor, entityTypePersistedInfo, commandContext) else throw new NoSuchElementException
}

object CursorStream {
  val storageType = MapStorage
}
