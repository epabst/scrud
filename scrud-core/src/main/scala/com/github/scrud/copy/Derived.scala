package com.github.scrud.copy

import com.github.scrud.{BaseFieldDeclaration, FieldDeclaration}
import com.github.scrud.context.RequestContext

/**
 * A [[Representation]] that is derived from other fields.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/25/14
 *         Time: 3:23 PM
 */
abstract class Derived[V](fields: BaseFieldDeclaration*) extends ExtensibleAdaptableField[V] with Representation[V]

object Derived {
  def apply[V,V1](field1: FieldDeclaration[V1])(derive: Option[V1] => Option[V]): Derived[V] = {
    new Derived[V](field1) {
      override def findSourceField(sourceType: SourceType) = {
        for {
          sourceField1 <- field1.toAdaptableField.findSourceField(sourceType)
        } yield new DerivedSourceField1(sourceField1)(derive)
      }

      override def findTargetField(targetType: TargetType) = None
    }
  }

  def apply[V,V1,V2](field1: FieldDeclaration[V1], field2: FieldDeclaration[V2])(derive: (Option[V1],Option[V2]) => Option[V]): Derived[V] = {
    new Derived[V](field1, field2) {
      override def findSourceField(sourceType: SourceType) = {
        for {
          sourceField1 <- field1.toAdaptableField.findSourceField(sourceType)
          sourceField2 <- field2.toAdaptableField.findSourceField(sourceType)
        } yield new DerivedSourceField2(sourceField1, sourceField2)(derive)
      }

      override def findTargetField(targetType: TargetType) = None
    }
  }

  def apply[V,V1,V2,V3](field1: FieldDeclaration[V1], field2: FieldDeclaration[V2], field3: FieldDeclaration[V3])
                       (derive: (Option[V1],Option[V2],Option[V3]) => Option[V]): Derived[V] = {
    new Derived[V](field1, field2, field3) {
      override def findSourceField(sourceType: SourceType) = {
        for {
          sourceField1 <- field1.toAdaptableField.findSourceField(sourceType)
          sourceField2 <- field2.toAdaptableField.findSourceField(sourceType)
          sourceField3 <- field3.toAdaptableField.findSourceField(sourceType)
        } yield new DerivedSourceField3(sourceField1, sourceField2, sourceField3)(derive)
      }

      override def findTargetField(targetType: TargetType) = None
    }
  }

  private class DerivedSourceField1[V,V1](field1: SourceField[V1])(derive: Option[V1] => Option[V]) extends SourceField[V] {
    /** Get some value or None from the given source. */
    override def findValue(source: AnyRef, context: RequestContext) = {
      val valueOpt1 = field1.findValue(source, context)
      derive.apply(valueOpt1)
    }
  }

  private class DerivedSourceField2[V,V1,V2](field1: SourceField[V1], field2: SourceField[V2])
                                            (derive: (Option[V1],Option[V2]) => Option[V]) extends SourceField[V] {
    /** Get some value or None from the given source. */
    override def findValue(source: AnyRef, context: RequestContext) = {
      val valueOpt1 = field1.findValue(source, context)
      val valueOpt2 = field2.findValue(source, context)
      derive.apply(valueOpt1,valueOpt2)
    }
  }

  private class DerivedSourceField3[V,V1,V2,V3](field1: SourceField[V1], field2: SourceField[V2], field3: SourceField[V3])
                                               (derive: (Option[V1],Option[V2],Option[V3]) => Option[V]) extends SourceField[V] {
    /** Get some value or None from the given source. */
    override def findValue(source: AnyRef, context: RequestContext) = {
      val valueOpt1 = field1.findValue(source, context)
      val valueOpt2 = field2.findValue(source, context)
      val valueOpt3 = field3.findValue(source, context)
      derive.apply(valueOpt1,valueOpt2,valueOpt3)
    }
  }
}
