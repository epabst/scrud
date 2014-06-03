package com.github.scrud.android.persistence

import com.github.scrud.platform.PersistenceAdaptableFieldFactory
import com.github.scrud.types._
import com.github.scrud.copy._
import android.database.Cursor
import com.github.scrud.FieldName
import com.github.scrud.EntityName
import android.content.ContentValues

/**
 * An AdaptableFieldFactory for SQLite fields on Android.
 */
object SQLiteAdaptableFieldFactory extends PersistenceAdaptableFieldFactory {
  override def sourceField[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V]): SourceField[V] = {
    new CursorSourceField(toPersistedFieldName(fieldName), PersistedType(qualifiedType))
  }

  override def targetField[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V]): TargetField[V] = {
    new ContentValuesTargetField[V](toPersistedFieldName(fieldName), PersistedType(qualifiedType))
  }

  def toPersistedFieldName(fieldName: FieldName): String = {
    SQLiteUtil.toNonReservedWord(fieldName.toSnakeCase)
  }

  class CursorSourceField[V](persistedFieldName: String, persistedType: PersistedType[V]) extends TypedSourceField[Cursor,V] {
    // This is a Weak Map so that when a Cursor is no longer used, the entry is automatically deleted.
//    private val weakColumnIndexByCursor = new JMapWrapper(new java.util.WeakHashMap[Cursor,Int](3))

    /** Get some value or None from the given source. */
    override def findFieldValue(cursor: Cursor, context: CopyContext): Option[V] = {
//      todo is this faster?    val columnIndex = weakColumnIndexByCursor.getOrElseUpdate(cursor, cursor.getColumnIndex(persistedFieldName))
      val columnIndex = cursor.getColumnIndex(persistedFieldName)
      persistedType.getValue(cursor, columnIndex)
    }
  }

  class ContentValuesTargetField[V](persistedFieldName: String, persistedType: PersistedType[V]) extends TypedTargetField[ContentValues,V] {
    /** Updates the {{{contentValues}}} target using the {{{valueOpt}}} for this field and some context. */
    override def updateFieldValue(contentValues: ContentValues, valueOpt: Option[V], context: CopyContext): ContentValues = {
      valueOpt match {
        case Some(value) => persistedType.putValue(contentValues, persistedFieldName, value)
        case None => contentValues.putNull(persistedFieldName)
      }
      contentValues
    }
  }
}
