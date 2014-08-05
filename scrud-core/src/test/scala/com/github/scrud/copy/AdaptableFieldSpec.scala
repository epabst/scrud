package com.github.scrud.copy

import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.platform.representation.{FieldLevel, DisplayUI, EditUI}
import org.scalatest.mock.MockitoSugar
import com.github.scrud.copy.types.{Default, MapStorage}

/**
 * A behavior specification for [[com.github.scrud.copy.AdaptableField]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 6/24/14
 */
@RunWith(classOf[JUnitRunner])
class AdaptableFieldSpec extends FunSpec with MustMatchers with MockitoSugar {
  val sourceField1 = mock[SourceField[String]]
  val sourceField2 = mock[SourceField[String]]
  val targetField1 = mock[TargetField[String]]
  val targetField2 = mock[TargetField[String]]

  describe("AdaptableField.apply") {
    it("must combine AdaptableFieldByType instances") {
      val field = AdaptableField[String](Seq(
        new AdaptableFieldByType(Seq(EditUI -> sourceField1), Seq(EditUI -> targetField1)),
        new AdaptableFieldByType(Seq(MapStorage -> sourceField2), Seq(DisplayUI(FieldLevel.Summary) -> targetField2))))
      field.isInstanceOf[AdaptableFieldByType[String]] must be (true)
      field.findSourceField(EditUI) must not be None
      field.findSourceField(MapStorage) must not be None
      field.findTargetField(EditUI) must not be None
      field.findTargetField(DisplayUI(FieldLevel.Summary)) must not be None
    }

    it("must combine AdaptableFieldByType instances even if already combined with others") {
      val composite = AdaptableField[String](Seq(
        AdaptableField[String](Seq(
          new AdaptableFieldByType(Seq(EditUI -> sourceField1), Seq(EditUI -> targetField1)),
          Default("hello"))),
        AdaptableField[String](Seq(
          new AdaptableFieldByType(Seq(MapStorage -> sourceField2), Seq(DisplayUI(FieldLevel.Summary) -> targetField2)),
          Default("hi")))))
      val CompositeAdaptableField(Seq(field: AdaptableFieldByType[String],
        Default(Some("hello")), Default(Some("hi")))) = composite
      field.findSourceField(EditUI) must not be None
      field.findSourceField(MapStorage) must not be None
      field.findTargetField(EditUI) must not be None
      field.findTargetField(DisplayUI(FieldLevel.Summary)) must not be None
    }
  }
}
