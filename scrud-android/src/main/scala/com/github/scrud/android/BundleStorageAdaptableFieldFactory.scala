package com.github.scrud.android

import com.github.scrud.copy._
import com.github.scrud.copy.AdaptableFieldWithRepresentations
import com.github.scrud.{FieldName, EntityName}
import com.github.scrud.types.QualifiedType
import com.github.scrud.platform.AdaptableFieldFactory
import android.os.Bundle
import com.github.scrud.android.persistence.{SQLiteUtil, PersistedType}
import com.github.scrud.platform.representation.{SerializedFormat, PersistenceRange}

/**
 * An [[com.github.scrud.platform.AdaptableFieldFactory]] for a [[android.os.Bundle]]
 * for storage type [[com.github.scrud.android.BundleStorage]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/11/14
 *         Time: 3:13 PM
 */
object BundleStorageAdaptableFieldFactory extends AdaptableFieldFactory {
  def adapt[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]): AdaptableFieldWithRepresentations[V] = {
    if (representations.contains(BundleStorage) ||
        representations.exists(_.isInstanceOf[PersistenceRange]) ||
        representations.exists(_.isInstanceOf[SerializedFormat])) {
      val persistedFieldName= SQLiteUtil.toNonReservedWord(fieldName.toSnakeCase)
      val storageField = new BundleStorageField[V](persistedFieldName, PersistedType(qualifiedType))
      AdaptableFieldWithRepresentations(new AdaptableFieldByType[V](
        Seq(BundleStorage -> storageField), Seq(BundleStorage -> storageField)), Set[Representation[V]](BundleStorage))
    } else {
      AdaptableFieldWithRepresentations.empty
    }
  }
}

private class BundleStorageField[V](persistedFieldName: String, persistedType: PersistedType[V]) extends TypedStorageField[Bundle,V] {
  /** Get some value or None from the given source. */
  override def findFieldValue(sourceData: Bundle, context: CopyContext): Option[V] = {
    persistedType.getValue(sourceData, persistedFieldName)
  }

  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  override def updateFieldValue(target: Bundle, valueOpt: Option[V], context: CopyContext): Bundle = {
    valueOpt match {
      case Some(value) => persistedType.putValue(target, persistedFieldName, value)
      case None => target.remove(persistedFieldName)
    }
    target
  }
}
