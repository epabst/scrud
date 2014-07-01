package com.github.scrud.persistence

import com.github.scrud.copy._
import com.github.scrud.platform.representation.Persistence
import com.github.scrud.platform.representation.PersistenceRange

/**
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 7/1/14
 */
class PersistenceRangeAdaptableField[V](persistenceRanges: Seq[PersistenceRange], sourceFieldOpt: Option[SourceField[V]],
                                        targetFieldOpt: Option[TargetField[V]]) extends ExtensibleAdaptableField[V] {
  def findSourceField(sourceType: SourceType) = {
    sourceType match {
      case persistence: Persistence if persistenceRanges.exists(_.includes(persistence)) =>
        sourceFieldOpt
      case _ =>
        None
    }
  }

  def findTargetField(targetType: TargetType) = {
    targetType match {
      case persistence: Persistence if persistenceRanges.exists(_.includes(persistence)) =>
        targetFieldOpt
      case _ =>
        None
    }
  }
}
