package com.github.scrud.android.persistence

import org.scalatest.FunSpec
import com.github.scrud.android.MyEntityType
import org.scalatest.matchers.MustMatchers

/**
 * A specification for [[com.github.scrud.android.persistence.EntityType]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 10/10/12
 * Time: 3:10 PM
 */
class EntityTypeSpec extends FunSpec with MustMatchers {
  describe("loadingValue") {
    it("must include loading values") {
      val loadingValue = MyEntityType.loadingValue
      loadingValue.update(Map.empty[String,Any]) must be (Map("name" -> "..."))
    }
  }
}
