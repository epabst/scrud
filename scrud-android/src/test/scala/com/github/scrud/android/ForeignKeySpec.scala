package com.github.scrud.android

import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import com.github.scrud.{EntityField, UriPath}
import org.scalatest.mock.EasyMockSugar
import persistence.SQLiteCriteria

/** A specification for [[com.github.scrud.android.ForeignKey]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
class ForeignKeySpec extends MustMatchers with EasyMockSugar {
  @Test
  def shouldGetCriteriaCorrectlyForForeignKey() {
    val foreign = ForeignKey[MyEntityType](MyEntity)
    val uri = UriPath(MyEntityType.entityName, 19)
    //add on extra stuff to make sure it is ignored
    val uriWithExtraStuff = uri / "foo" / 1234
    val criteria = foreign.copyAndUpdate(uriWithExtraStuff, new SQLiteCriteria)
    criteria.selection must be (List(EntityField.fieldName(MyEntity) + "=19"))
  }
}