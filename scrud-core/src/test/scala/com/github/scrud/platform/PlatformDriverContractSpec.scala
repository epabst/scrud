package com.github.scrud.platform

import org.scalatest.FunSpec
import com.github.scrud.copy.{TargetType, SourceType}
import com.github.scrud.types.TitleQT
import org.scalatest.matchers.MustMatchers
import com.github.scrud.platform.representation._
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
        val representations = Seq[Representation](MapStorage, Persistence, XmlFormat, JsonFormat, EditUI)
        val field = platformDriver.field(EntityName("Foo"), "foo", TitleQT, representations)
        for (sourceType <- representations.collect { case s: SourceType => s }) {
          withClue(sourceType) {
            field.findSourceField(sourceType).isDefined must be (true)
          }
        }
      }
  
      it("must not return a field for a SourceType not provided in the FieldApplicability") {
        val platformDriver = makePlatformDriver()
        val representations = Seq[Representation](Persistence, XmlFormat)
        val field = platformDriver.field(EntityName("Foo"), "foo", TitleQT, representations)
        field.findSourceField(EditUI) must be (None)
      }
    }
    
    describe("findTargetField") {  
      it("must support the basic SourceTypes") {
        val platformDriver = makePlatformDriver()
        val representations = Seq[Representation](MapStorage, Persistence, XmlFormat, JsonFormat, EditUI, SelectUI, SummaryUI, DetailUI)
        val field = platformDriver.field(EntityName("Foo"), "foo", TitleQT, representations)
        for (targetType <- representations.collect { case t: TargetType => t }) {
          withClue(targetType) {
            field.findTargetField(targetType).isDefined must be (true)
          }
        }
      }

      it("must not return a field for a SourceType not provided in the FieldApplicability") {
        val platformDriver = makePlatformDriver()
        val representations = Seq[Representation](Persistence, XmlFormat)
        val field = platformDriver.field(EntityName("Foo"), "foo", TitleQT, representations)
        field.findTargetField(EditUI) must be (None)
      }
    }
  }
}
