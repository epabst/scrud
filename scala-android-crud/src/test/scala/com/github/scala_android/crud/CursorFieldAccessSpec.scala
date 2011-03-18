package com.github.scala_android.crud

import android.provider.BaseColumns
import org.junit.Test
import org.junit.runner.RunWith
import com.xtremelabs.robolectric.RobolectricTestRunner
import com.github.triangle._
import Field._
import CursorFieldAccess._
import org.scalatest.matchers.ShouldMatchers
import android.content.Intent
import android.net.Uri

/**
 * A specification for {@link CursorFieldAccess}.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 2/18/11
 * Time: 6:22 PM
 */
@RunWith(classOf[RobolectricTestRunner])
class CursorFieldAccessSpec extends ShouldMatchers {
  @Test
  def shouldGetColumnsForQueryCorrectly {
    val foreign = foreignKey(MyCrudEntityTypeRef)
    val fields = List(Field(foreign), Field(persisted[Long]("age")))
    val actualFields = CursorFieldAccess.queryFieldNames(fields)
    actualFields should be (List(BaseColumns._ID, foreign.fieldName, "age"))
  }

  @Test
  def shouldGetCriteriaCorrectly {
    val field = Field[Long](sqliteCriteria("age"), default(19))
    val criteria = new SQLiteCriteria
    field.copy(Unit, criteria)
    criteria.selection should be ("age=19")
  }

  @Test
  def shouldGetCriteriaCorrectlyForForeignKey {
    val foreign = foreignKey(MyCrudEntityTypeRef)
    val field = Field[Long](foreign)
    val uri = EntityUriSegment(MyCrudEntityTypeRef.entityName, "19").specifyInUri(Uri.EMPTY)
    //add on extra stuff to make sure it is ignored
    val intent = new Intent("", Uri.withAppendedPath(uri, "foo/1234"))
    val criteria = new SQLiteCriteria
    field.copy(intent, criteria)
    criteria.selection should be (foreign.fieldName + "=19")
  }
}