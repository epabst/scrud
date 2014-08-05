package com.github.scrud.platform

import com.github.scrud.persistence.{PersistenceFactory, PersistenceRangeAdaptableField, ListBufferPersistenceFactoryForTesting}
import com.github.scrud.types.QualifiedType
import com.github.scrud.{FieldName, EntityName}
import com.github.scrud.copy.{AdaptableFieldWithRepresentations, Representation}
import com.github.scrud.platform.representation.PersistenceRange

/**
 * A simple PlatformDriver for testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/28/12
 *         Time: 1:27 PM
 */
class TestingPlatformDriver extends StubPlatformDriver {
  override val localDatabasePersistenceFactory: PersistenceFactory = ListBufferPersistenceFactoryForTesting

  private object PersistenceFieldFactory extends AdaptableFieldFactory {
    override def adapt[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V],
                          representations: Seq[Representation[V]]): AdaptableFieldWithRepresentations[V] = {
      val persistenceRanges = representations.collect { case persistenceRange: PersistenceRange => persistenceRange }
      val sourceField = UniversalMapStorageAdaptableFieldFactory.createSourceField(entityName, fieldName, qualifiedType)
      val targetField = UniversalMapStorageAdaptableFieldFactory.createTargetField(entityName, fieldName, qualifiedType)
      val adaptableField = new PersistenceRangeAdaptableField[V](persistenceRanges, Some(sourceField), Some(targetField))
      AdaptableFieldWithRepresentations(adaptableField, persistenceRanges.toSet[Representation[V]])
    }
  }

  override val platformSpecificFieldFactories: Seq[AdaptableFieldFactory] = Seq(UniversalMapStorageAdaptableFieldFactory, PersistenceFieldFactory)
}

object TestingPlatformDriver extends TestingPlatformDriver
