package com.github.scrud.platform

import org.scalatest.FunSpec
import com.github.scrud.copy.{TargetType, SourceType, FieldApplicability}
import com.github.scrud.types.TitleQT
import com.github.scrud.platform.node._
import org.scalatest.matchers.MustMatchers
import com.github.scrud.EntityName

/**
 * A specification of the contract that every [[com.github.scrud.platform.PlatformDriver]] must comply with.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/12/13
 *         Time: 10:54 AM
 */
abstract class PlatformDriverContractSpec extends FunSpec with MustMatchers {
  protected def makePlatformDriver(): PlatformDriver

  describe("field") {
    describe("findSourceField") {
      it("must support the basic SourceTypes") {
        val platformDriver = makePlatformDriver()
        val basicSourceTypes = Set[SourceType](MapStorage, Persistence, XmlFormat, JsonFormat, EditUI)
        val applicability = FieldApplicability(from = basicSourceTypes, to = Set.empty)
        val field = platformDriver.field("foo", TitleQT, applicability, EntityName("Foo"))
        basicSourceTypes.foreach { sourceType =>
          withClue(sourceType) {
            field.findSourceField(sourceType).isDefined must be (true)
          }
        }
      }
  
      it("must not return a field for a SourceType not provided in the FieldApplicability") {
        val platformDriver = makePlatformDriver()
        val applicability = FieldApplicability(from = Set(Persistence, XmlFormat), to = Set.empty)
        val field = platformDriver.field("foo", TitleQT, applicability, EntityName("Foo"))
        field.findSourceField(EditUI) must be (None)
      }
    }
    
    describe("findTargetField") {  
      it("must support the basic SourceTypes") {
        val platformDriver = makePlatformDriver()
        val basicTargetTypes = Set[TargetType](MapStorage, Persistence, XmlFormat, JsonFormat, EditUI, SummaryUI, DetailUI)
        val applicability = FieldApplicability(from = Set.empty, to = basicTargetTypes)
        val field = platformDriver.field("foo", TitleQT, applicability, EntityName("Foo"))
        basicTargetTypes.foreach { targetType =>
          withClue(targetType) {
            field.findTargetField(targetType).isDefined must be (true)
          }
        }
      }

      it("must not return a field for a SourceType not provided in the FieldApplicability") {
        val platformDriver = makePlatformDriver()
        val applicability = FieldApplicability(from = Set.empty, to = Set(Persistence, XmlFormat))
        val field = platformDriver.field("foo", TitleQT, applicability, EntityName("Foo"))
        field.findTargetField(EditUI) must be (None)
      }
    }
  }
}
