package com.github.scrud.android.persistence

import android.provider.BaseColumns
import org.junit.Test
import org.junit.runner.RunWith
import com.github.triangle._
import PortableField._
import CursorField._
import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.MockitoSugar
import android.database.Cursor
import com.github.scrud.platform.PlatformTypes._
import android.content.ContentValues
import org.mockito.Mockito._
import com.github.scrud.android.CustomRobolectricTestRunner

/** A specification for [[com.github.scrud.android.persistence.CursorField]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
class CursorFieldSpec extends MustMatchers with MockitoSugar {
  @Test
  def shouldGetColumnsForQueryCorrectly() {
    val foreign = persisted[ID]("foreignID")
    val combined = persisted[Float]("height") + default(6.0f)
    val fields = FieldList(CursorField.PersistedId, foreign, persisted[Int]("age"), combined)
    val actualFields = CursorField.queryFieldNames(fields)
    actualFields must be (List(BaseColumns._ID, "foreignID", "age", "height"))
  }

  @Test
  def persistedShouldReturnNoneIfNullInCursor() {
    val cursor = mock[Cursor]
    when(cursor.getColumnIndex("name")).thenReturn(1)
    when(cursor.isNull(1)).thenReturn(true)
    val field = persisted[String]("name")
    field.getValue(cursor) must be (None)
  }

  @Test
  def persistedFieldShouldNotBeDefinedIfColumnNotInCursor() {
    val cursor = mock[Cursor]
    when(cursor.getColumnIndex("name")).thenReturn(-1)
    val field = persisted[String]("name")
    field.getterVal.isDefinedAt(GetterInput.single(cursor)) must be (false)
  }

  @Test
  def persistedFieldShouldNotBeDefinedIfColumnNotInContentValues() {
    val contentValues = mock[ContentValues]
    when(contentValues.containsKey("name")).thenReturn(false)
    val field = persisted[String]("name")
    field.getterVal.isDefinedAt(GetterInput.single(contentValues)) must be (false)
  }

  @Test
  def persistedShouldPutNullIntoContentValuesForNoValue() {
    val contentValues = mock[ContentValues]
    val field = persisted[String]("name")
    field.updateWithValue(contentValues, None)
    verify(contentValues).putNull("name")
  }

  @Test
  def persistedShouldNotPutAnythingIntoContentValuesForUndefined() {
    val contentValues = mock[ContentValues]
    val field = persisted[String]("name")
    field.copyAndUpdate(new Object, contentValues)
    verifyNoMoreInteractions(contentValues)
  }

  @Test
  def shouldGetCriteriaCorrectlyForANumber() {
    val field = sqliteCriteria[Int]("age") + default(19)
    val criteria: SQLiteCriteria = field.copyAndUpdate(PortableField.UseDefaults, new SQLiteCriteria)
    criteria.selection must be (List("age=19"))
  }

  @Test
  def shouldGetCriteriaCorrectlyForAString() {
    val field = sqliteCriteria[String]("name") + default("John Doe")
    val criteria: SQLiteCriteria = field.copyAndUpdate(PortableField.UseDefaults, new SQLiteCriteria)
    criteria.selection must be (List("name=\"John Doe\""))
  }

  @Test
  def shouldHandleMultipleSelectionCriteria() {
    val field = sqliteCriteria[Int]("age") + sqliteCriteria[Int]("alternateAge") + default(19)
    val criteria: SQLiteCriteria = field.copyAndUpdate(PortableField.UseDefaults, new SQLiteCriteria)
    criteria.selection.sorted must be (List("age=19", "alternateAge=19"))
  }
}