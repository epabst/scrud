package com.github.scrud.android.persistence

import org.junit.runner.RunWith
import com.github.scrud.android.{EntityTypeForTesting, CustomRobolectricTestRunner}
import org.junit.Test
import com.github.triangle.{PortableField, GetterInput}
import org.scalatest.matchers.MustMatchers

/**
 * A behavior specification for [[com.github.scrud.android.persistence.SQLiteCriteria]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 8/7/13
 *         Time: 3:22 PM
 */
@RunWith(classOf[CustomRobolectricTestRunner])
class SQLiteCriteriaSpec extends MustMatchers {
  @Test
  def itMustBeCreatableFromAnEntityType() {
    val entityType = new EntityTypeForTesting()
    val uri = entityType.toUri(51) / "Book"
    val getterInput = GetterInput(uri, PortableField.UseDefaults)
    val initialCriteria = new SQLiteCriteria(orderBy = Some(CursorField.idFieldName + " desc"))
    val resultCriteria = entityType.copyAndUpdate(getterInput, initialCriteria)
    resultCriteria must be (SQLiteCriteria(List("_id=51"), orderBy = Some("_id desc")))
  }
}
