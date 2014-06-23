package com.github.scrud.android.persistence

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import android.database.Cursor
import com.github.scrud.android.{EntityTypeForTesting, AndroidCommandContextForTesting}
import com.github.scrud.persistence.EntityTypeMapForTesting
import com.github.scrud.platform.representation.Persistence

/** A behavior specification for [[com.github.scrud.persistence.EntityPersistence]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class CursorStreamSpec extends FunSpec with MustMatchers with MockitoSugar {
  it("must handle an empty Cursor") {
    val entityType = EntityTypeForTesting
    val cursor = mock[Cursor]
    stub(cursor.moveToNext()).toReturn(false)
    val commandContext = new AndroidCommandContextForTesting(new EntityTypeMapForTesting(entityType))
    val stream = CursorStream(cursor, EntityTypePersistedInfo(entityType), commandContext)
    stream.isEmpty must be (true)
    stream.size must be (0)
    stream.headOption must be (None)
  }

  it("must not instantiate the entire Stream for an infinite Cursor") {
    val entityType = EntityTypeForTesting
    val cursor = mock[Cursor]
    stub(cursor.moveToNext).toReturn(true)
    stub(cursor.getColumnIndex("name")).toReturn(1)
    stub(cursor.getString(1)).toReturn("Bryce")

    val commandContext = new AndroidCommandContextForTesting(new EntityTypeMapForTesting(entityType))
    val stream = CursorStream(cursor, EntityTypePersistedInfo(entityType), commandContext)
    val second = stream.tail.head
    entityType.name.getRequired(Persistence.Latest, second, entityType.toUri, commandContext) must be ("Bryce")
  }

  it("must have correct number of elements") {
    val entityType = EntityTypeForTesting
    val cursor = mock[Cursor]
    when(cursor.moveToNext).thenReturn(true).thenReturn(true).thenReturn(false)
    stub(cursor.getColumnIndex("name")).toReturn(1)
    stub(cursor.getString(1)).toReturn("Allen")

    val commandContext = new AndroidCommandContextForTesting(new EntityTypeMapForTesting(entityType))
    val stream = CursorStream(cursor, EntityTypePersistedInfo(entityType), commandContext)
    stream.toList.size must be (2)
  }

  it("must have correct size") {
    val entityType = EntityTypeForTesting
    val cursor = mock[Cursor]
    stub(cursor.getCount).toReturn(500)

    val commandContext = new AndroidCommandContextForTesting(new EntityTypeMapForTesting(entityType))
    val stream = CursorStream(cursor, EntityTypePersistedInfo(entityType), commandContext)
    stream.size must be (500)
    stream.length must be (500)
  }

  it("must allow accessing data from different positions in any order") {
    val entityType = EntityTypeForTesting
    val cursor = mock[Cursor]
    when(cursor.moveToNext).thenReturn(true).thenReturn(true).thenReturn(false)
    stub(cursor.getColumnIndex("name")).toReturn(1)
    when(cursor.getString(1)).thenReturn("Allen").thenReturn("Bryce")

    val commandContext = new AndroidCommandContextForTesting(new EntityTypeMapForTesting(entityType))
    val stream = CursorStream(cursor, EntityTypePersistedInfo(entityType), commandContext)
    val second = stream.tail.head
    val first = stream.head
    entityType.name.getRequired(Persistence.Latest, second, entityType.toUri, commandContext) must be ("Bryce")
    entityType.name.getRequired(Persistence.Latest, first, entityType.toUri, commandContext) must be ("Allen")
  }
}
