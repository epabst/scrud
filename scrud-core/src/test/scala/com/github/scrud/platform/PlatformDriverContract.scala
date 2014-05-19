package com.github.scrud.platform

import org.scalatest.FunSpec
import com.github.scrud.copy.{Representation, TargetType, SourceType}
import com.github.scrud.types.TitleQT
import org.scalatest.matchers.MustMatchers
import com.github.scrud.platform.representation._
import com.github.scrud.{FieldName, EntityType, EntityName}
import com.github.scrud.copy.types.{Default, MapStorage}

/**
 * A specification of the contract that every [[com.github.scrud.platform.PlatformDriver]] must comply with.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/12/13
 *         Time: 10:54 AM
 */
abstract class PlatformDriverContract extends FunSpec with MustMatchers {
  protected def makePlatformDriver(): PlatformDriver

  describe("field") {
    describe("findSourceField") {
      it("must support the basic SourceTypes") {
        val platformDriver = makePlatformDriver()
        val representations = Seq[Representation[Nothing]](MapStorage, Persistence(1), XmlFormat, JsonFormat, EditUI)
        val field = platformDriver.field(EntityName("Foo"), FieldName("foo"), TitleQT, representations)
        for (sourceType <- representations.collect { case s: SourceType => s }) {
          withClue(sourceType) {
            field.findSourceField(sourceType).isDefined must be (true)
          }
        }
      }
  
      it("must not return a field for a SourceType not provided in the FieldApplicability") {
        val platformDriver = makePlatformDriver()
        val representations = Seq[Representation[Nothing]](Persistence(1), XmlFormat)
        val field = platformDriver.field(EntityName("Foo"), FieldName("foo"), TitleQT, representations)
        field.findSourceField(EditUI) must be (None)
      }

      it("must ignore unknown SourceTypes and TargetTypes") {
        object UnknownRepresentation extends Representation[Nothing]
        val platformDriver = makePlatformDriver()
        val representations = Seq[Representation[Nothing]](MapStorage, UnknownRepresentation, JsonFormat)
        val field = platformDriver.field(EntityName("Foo"), FieldName("foo"), TitleQT, representations)
        field.findSourceField(MapStorage).isDefined must be (true)
        field.findSourceField(JsonFormat).isDefined must be (true)
        //Shouldn't compile: field.findSourceField(UnknownRepresentation).isDefined must be (false)
      }
    }
    
    describe("findTargetField") {  
      it("must support the basic SourceTypes") {
        val platformDriver = makePlatformDriver()
        val representations = Seq[Representation[Nothing]](MapStorage, Persistence(1), XmlFormat, JsonFormat, EditUI, SelectUI, SummaryUI, DetailUI)
        val field = platformDriver.field(EntityName("Foo"), FieldName("foo"), TitleQT, representations)
        for (targetType <- representations.collect { case t: TargetType => t }) {
          withClue(targetType) {
            field.findTargetField(targetType).isDefined must be (true)
          }
        }
      }

      it("must not return a field for a SourceType not provided in the FieldApplicability") {
        val platformDriver = makePlatformDriver()
        val representations = Seq[Representation[Nothing]](Persistence(1), XmlFormat)
        val field = platformDriver.field(EntityName("Foo"), FieldName("foo"), TitleQT, representations)
        field.findTargetField(EditUI) must be (None)
      }
    }
  }

  describe("calculateDataVersion") {
    it("must find the maximum of the dataVersions used (when no maxDataVersions are used)") {
      val platformDriver = makePlatformDriver()
      val entityName = EntityName("Foo")
      val entityType = new EntityType(entityName, platformDriver) {
        field("bar1", TitleQT, Seq(Persistence(3), Default("bar1")))
        field("bar3", TitleQT, Seq(Persistence(1), Default("bar3")))
        field("bar4", TitleQT, Seq(Persistence(2), Default("bar4")))
      }
      platformDriver.calculateDataVersion(Seq(entityType)) must be (3)
    }

    it("must use one more than the maximum of the maxDataVersions used") {
      val platformDriver = makePlatformDriver()
      val entityName = EntityName("Foo")
      val entityType = new EntityType(entityName, platformDriver) {
        field("bar1", TitleQT, Seq(Persistence(3), Default("bar1")))
        field("bar3", TitleQT, Seq(Persistence(1, 4), Default("bar3")))
        field("bar4", TitleQT, Seq(Persistence(2, 3), Default("bar4")))
      }
      platformDriver.calculateDataVersion(Seq(entityType)) must be (5)
    }
  }
}
