package com.github.scrud.android.persistence

import com.github.scrud.platform.AdaptableFieldFactory
import com.github.scrud.types._
import com.github.scrud.copy._
import android.database.Cursor
import com.github.scrud.FieldName
import com.github.scrud.EntityName
import android.content.ContentValues
import com.github.scrud.platform.representation.{SerializedFormat, PersistenceRange}
import com.github.scrud.persistence.PersistenceRangeAdaptableField

/**
 * An AdaptableFieldFactory for ContentProvider/ContentResolver (such as for SQLite) fields on Android.
 */
object AndroidContentAdaptableFieldFactory extends AdaptableFieldFactory {
  def adapt[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]) = {
    if (representations.contains(ContentValuesStorage) ||
      representations.exists(_.isInstanceOf[PersistenceRange]) ||
      representations.exists(_.isInstanceOf[SerializedFormat])) {
      val persistedFieldName = toPersistedFieldName(fieldName)

      val persistenceRanges = representations.collect { case persistenceRange: PersistenceRange => persistenceRange }
      val cursorSourceField = new CursorSourceField(persistedFieldName, PersistedType(qualifiedType))
      val contentValuesSourceField = new ContentValuesSourceField[V](persistedFieldName, PersistedType(qualifiedType))
      val contentValuesTargetField = new ContentValuesTargetField[V](persistedFieldName, PersistedType(qualifiedType))
      val adaptableFieldByType = new AdaptableFieldByType[V](
        Seq(ContentValuesStorage -> contentValuesSourceField), Seq(ContentValuesStorage -> contentValuesTargetField))
      val persistenceRangeAdaptableField = new PersistenceRangeAdaptableField[V](persistenceRanges, Some(cursorSourceField), Some(contentValuesTargetField))

      AdaptableFieldWithRepresentations(adaptableFieldByType.orElse(persistenceRangeAdaptableField),
        persistenceRanges.toSet[Representation[V]] + ContentValuesStorage)
    } else {
      AdaptableFieldWithRepresentations.empty
    }
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
      if (columnIndex < 0) {
        throw new IllegalArgumentException("column=" + persistedFieldName + " does not exist in cursor=" + cursor)
      }
      persistedType.getValue(cursor, columnIndex)
    }
  }

  class ContentValuesSourceField[V](persistedFieldName: String, persistedType: PersistedType[V]) extends TypedSourceField[ContentValues,V] {
    /** Get some value or None from the given source. */
    override def findFieldValue(sourceData: ContentValues, context: CopyContext): Option[V] = {
      persistedType.getValue(sourceData, persistedFieldName)
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
