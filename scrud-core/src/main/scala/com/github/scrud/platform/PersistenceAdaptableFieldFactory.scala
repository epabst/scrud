package com.github.scrud.platform

import com.github.scrud.copy._
import com.github.scrud.types.QualifiedType
import com.github.scrud.copy.AdaptableFieldWithRepresentations
import com.github.scrud.EntityName
import com.github.scrud.platform.representation.{Persistence, PersistenceRange}
import scala.Some

/**
 * An AdaptableFieldFactory for [[com.github.scrud.platform.representation.Persistence]]
 * and [[com.github.scrud.platform.representation.PersistenceRange]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/12/14
 *         Time: 8:21 AM
 */
abstract class PersistenceAdaptableFieldFactory extends AdaptableFieldFactory {
  def adapt[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V], representations: Seq[Representation[V]]) = {
    val persistenceRanges = representations.collect {
      case persistenceRange: PersistenceRange => persistenceRange
    }
    val someSourceField = Some(sourceField(entityName, fieldName, qualifiedType))
    val someTargetField = Some(targetField(entityName, fieldName, qualifiedType))
    val adaptableField = new ExtensibleAdaptableField[V] {
      def findSourceField(sourceType: SourceType) = {
        sourceType match {
          case persistence: Persistence if persistenceRanges.exists(_.includes(persistence)) =>
            someSourceField
          case _ =>
            None
        }
      }

      def findTargetField(targetType: TargetType) = {
        targetType match {
          case persistence: Persistence if persistenceRanges.exists(_.includes(persistence)) =>
            someTargetField
          case _ =>
            None
        }
      }
    }
    AdaptableFieldWithRepresentations(adaptableField, persistenceRanges.toSet)
  }
  
  def sourceField[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V]): SourceField[V]
  
  def targetField[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V]): TargetField[V]
}
