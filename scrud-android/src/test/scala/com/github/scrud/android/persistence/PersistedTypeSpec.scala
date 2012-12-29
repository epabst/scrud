package com.github.scrud.android.persistence

import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.mock.EasyMockSugar
import org.junit.Test
import android.os.Bundle
import com.github.scrud.android.CustomRobolectricTestRunner


/** A behavior specification for [[com.github.scrud.android.persistence.PersistedType]].
  * @author Eric Pabst (epabst@gmail.com)
  */

@RunWith(classOf[CustomRobolectricTestRunner])
class PersistedTypeSpec extends MustMatchers with EasyMockSugar {
  @Test
  def itMustReadAndWriteBundle() {
    import PersistedType._
    verifyPersistedTypeWithBundle("hello")
    verifyPersistedTypeWithBundle(100L)
  }

  def verifyPersistedTypeWithBundle[T](value: T)(implicit persistedType: PersistedType[T]) {
    val bundle = new Bundle()
    persistedType.putValue(bundle, "foo", value)
    persistedType.getValue(bundle, "foo") must be (Some(value))
  }

  @Test
  def itMustGiveCorrectSQLiteType() {
    import PersistedType._
    stringType.sqliteType must be ("TEXT")
    blobType.sqliteType must be ("BLOB")
    longRefType.sqliteType must be ("INTEGER")
    longType.sqliteType must be ("INTEGER")
    intRefType.sqliteType must be ("INTEGER")
    intType.sqliteType must be ("INTEGER")
    shortRefType.sqliteType must be ("INTEGER")
    shortType.sqliteType must be ("INTEGER")
    byteRefType.sqliteType must be ("INTEGER")
    byteType.sqliteType must be ("INTEGER")
    doubleRefType.sqliteType must be ("REAL")
    doubleType.sqliteType must be ("REAL")
    floatRefType.sqliteType must be ("REAL")
    floatType.sqliteType must be ("REAL")
  }
}