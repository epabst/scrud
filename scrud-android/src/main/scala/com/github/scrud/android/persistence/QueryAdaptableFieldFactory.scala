package com.github.scrud.android.persistence

import com.github.scrud.copy._
import com.github.scrud.copy.AdaptableFieldWithRepresentations
import com.github.scrud.{FieldName, EntityName}
import com.github.scrud.types.QualifiedType
import com.github.scrud.platform.AdaptableFieldFactory
import com.github.scrud.platform.representation.{Query, PersistenceRange}

/**
 * An [[com.github.scrud.platform.AdaptableFieldFactory]] for a [[SQLiteCriteria]]
 * for target type [[com.github.scrud.platform.representation.Query]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/11/14
 *         Time: 3:13 PM
 */
object QueryAdaptableFieldFactory extends AdaptableFieldFactory {
  def adapt[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]): AdaptableFieldWithRepresentations[V] = {
    if (representations.contains(Query) ||
        representations.exists(_.isInstanceOf[PersistenceRange])) {
      val persistedFieldName= SQLiteUtil.toNonReservedWord(fieldName.toSnakeCase)
      val targetField = new QueryField[V](persistedFieldName, PersistedType(qualifiedType))
      AdaptableFieldWithRepresentations(new AdaptableFieldByType[V](
        Seq.empty, Seq(Query -> targetField)), Set[Representation[V]](Query))
    } else {
      AdaptableFieldWithRepresentations.empty
    }
  }
}

private class QueryField[V](persistedFieldName: String, persistedType: PersistedType[V]) extends TypedTargetField[SQLiteCriteria,V] {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  override def updateFieldValue(criteria: SQLiteCriteria, valueOpt: Option[V], context: CopyContext): SQLiteCriteria = {
    valueOpt match {
      case Some(value) =>
        val formattedValue = value match {
          case s: String => "\"" + s + "\""
          case n => n.toString
        }
        criteria.copy(selection = (persistedFieldName + "=" + formattedValue) +: criteria.selection)
      case None =>
        // don't add anything to the criteria
        criteria
    }
  }
}
