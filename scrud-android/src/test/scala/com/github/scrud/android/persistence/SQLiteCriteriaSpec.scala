package com.github.scrud.android.persistence

import org.junit.runner.RunWith
import com.github.scrud.android.{AndroidCommandContextForTesting, EntityTypeForTesting, CustomRobolectricTestRunner}
import org.junit.Test
import org.scalatest.matchers.MustMatchers
import com.github.scrud.platform.representation.Query
import com.github.scrud.persistence.EntityTypeMapForTesting
import com.github.scrud.copy.SourceType

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
    val commandContext = new AndroidCommandContextForTesting(new EntityTypeMapForTesting(entityType))
    val initialCriteria = new SQLiteCriteria(orderBy = Some(entityType.idFieldName + " desc"))
    val resultCriteria = entityType.copyAndUpdate(SourceType.none, SourceType.none, uri, Query, initialCriteria, commandContext)
    resultCriteria must be (SQLiteCriteria(List("_id=51"), orderBy = Some("_id desc")))
  }
}
