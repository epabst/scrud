package com.github.scrud.android.persistence

import com.github.scrud.copy._
import com.github.scrud.copy.AdaptableFieldWithRepresentations
import com.github.scrud.{FieldName, EntityName}
import com.github.scrud.types.QualifiedType
import com.github.scrud.platform.AdaptableFieldFactory
import com.github.scrud.platform.representation.{SerializedFormat, PersistenceRange}
import android.content.ContentValues

/**
 * An [[com.github.scrud.platform.AdaptableFieldFactory]] for a [[android.content.ContentValues]]
 * for source type [[com.github.scrud.android.persistence.ContentValuesStorage]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 6/27/14
 */
object ContentValuesStorageAdaptableFieldFactory extends AdaptableFieldFactory {
  def adapt[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]): AdaptableFieldWithRepresentations[V] = {
    if (representations.contains(ContentValuesStorage) ||
        representations.exists(_.isInstanceOf[PersistenceRange]) ||
        representations.exists(_.isInstanceOf[SerializedFormat])) {
      val persistedFieldName= SQLiteUtil.toNonReservedWord(fieldName.toSnakeCase)
      val sourceField = new ContentValuesSourceField[V](persistedFieldName, PersistedType(qualifiedType))
      AdaptableFieldWithRepresentations(new AdaptableFieldByType[V](
        Seq(ContentValuesStorage -> sourceField), Seq.empty), Set[Representation[V]](ContentValuesStorage))
    } else {
      AdaptableFieldWithRepresentations.empty
    }
  }
}

private class ContentValuesSourceField[V](persistedFieldName: String, persistedType: PersistedType[V]) extends TypedSourceField[ContentValues,V] {
  /** Get some value or None from the given source. */
  override def findFieldValue(sourceData: ContentValues, context: CopyContext): Option[V] = {
    persistedType.getValue(sourceData, persistedFieldName)
  }
}
