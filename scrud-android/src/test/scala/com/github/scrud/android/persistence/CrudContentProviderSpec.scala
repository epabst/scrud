package com.github.scrud.android.persistence

import org.junit.runner.RunWith
import com.github.scrud.util.CrudMockitoSugar
import org.scalatest.matchers.MustMatchers
import org.junit.Test
import com.github.scrud.{CrudApplication, UriPath, EntityName}
import com.github.scrud.android.view.AndroidConversions._
import android.content.{ContentValues, ContentResolver}
import com.github.scrud.android._
import com.github.scrud.state.State
import com.github.scrud.persistence.ListBufferPersistenceFactory
import scala.Some
import com.github.scrud.android.CrudApplicationForTesting
import view.AndroidConversions
import android.net.Uri

/**
 * A behavior specification for [[com.github.scrud.android.persistence.CrudContentProvider]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/18/13
 * Time: 4:59 PM
 */
@RunWith(classOf[CustomRobolectricTestRunner])
class CrudContentProviderSpec extends CrudMockitoSugar with MustMatchers {
  val platformDriver = new AndroidPlatformDriver(null)
  val fooEntityName = EntityName("Foo")
  val fooEntityType = new EntityTypeForTesting(fooEntityName, platformDriver)
  val fooCrudType = new CrudTypeForTesting(fooEntityType, new ListBufferPersistenceFactory[Map[String,Option[Any]]](Map.empty))
  val barEntityName = EntityName("Bar")
  val barEntityType = new EntityTypeForTesting(barEntityName, platformDriver)
  val barCrudType = new CrudTypeForTesting(barEntityType, new ListBufferPersistenceFactory[Map[String,Option[Any]]](Map.empty))
  val testApplication = new CrudApplicationForTesting(fooCrudType, barCrudType)

  private def toUri(uriPath: UriPath): Uri = AndroidConversions.toUri(uriPath, testApplication)

  @Test
  def getType_mustUseLastEntityName() {
    val provider = new CrudContentProviderForTesting(testApplication)
    provider.getType(toUri(fooEntityName.toUri(3))) must be (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + authorityFor(testApplication) + "." + fooEntityName)
    provider.getType(toUri(UriPath(fooEntityName))) must be (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + authorityFor(testApplication) + "." + fooEntityName)
  }

  @Test
  def query_mustReturnMultipleRows() {
    val provider = new CrudContentProviderForTesting(testApplication)
    val data1 = Map("name" -> Some("George"), "age" -> Some(31), "uri" -> None)
    provider.insert(toUri(UriPath(fooEntityName)), fooEntityType.copyAndUpdate(data1, new ContentValues()))
    val data2 = Map("name" -> Some("Wilma"), "age" -> Some(30), "uri" -> None)
    provider.insert(toUri(UriPath(fooEntityName)), fooEntityType.copyAndUpdate(data2, new ContentValues()))
    val cursor = provider.query(toUri(UriPath(fooEntityName)), Array.empty, null, Array.empty, null)
    cursor.getCount must be (2)
    CursorStream(cursor, EntityTypePersistedInfo(fooEntityType)).toList.map(_ - "_id") must be (List(data2, data1))
  }

  @Test
  def update_mustModifyTheData() {
    val provider = new CrudContentProviderForTesting(testApplication)
    val data1 = Map("name" -> Some("George"), "age" -> Some(31), "uri" -> None)
    val uri1 = provider.insert(toUri(UriPath(fooEntityName)), fooEntityType.copyAndUpdate(data1, new ContentValues()))
    val id1 = toUriPath(uri1).findId(fooEntityName).get
    val data2 = Map("name" -> Some("Wilma"), "age" -> Some(30), "uri" -> None)
    provider.insert(toUri(UriPath(fooEntityName)), fooEntityType.copyAndUpdate(data2, new ContentValues()))
    val data1b = Map("name" -> Some("Greg"), "age" -> Some(32), "uri" -> None, CursorField.idFieldName -> Some(id1))
    provider.update(uri1, fooEntityType.copyAndUpdate(data1b, new ContentValues()), null, Array.empty)
    val cursor = provider.query(toUri(UriPath(fooEntityName)), Array.empty, null, Array.empty, null)
    val results = CursorStream(cursor, EntityTypePersistedInfo(fooEntityType)).toList
    results.size must be (2)
    results.map(_ - "_id") must be (List(data2, data1b - "_id"))
  }

  @Test
  def delete_mustDelete() {
    val provider = new CrudContentProviderForTesting(testApplication)
    val data1 = Map("name" -> Some("George"), "age" -> Some(31), "uri" -> None)
    val uri1 = provider.insert(toUri(UriPath(fooEntityName)), fooEntityType.copyAndUpdate(data1, new ContentValues()))
    val data2 = Map("name" -> Some("Wilma"), "age" -> Some(30), "uri" -> None)
    val uri2 = provider.insert(toUri(UriPath(fooEntityName)), fooEntityType.copyAndUpdate(data2, new ContentValues()))
    provider.delete(uri1, null, Array.empty) must be (1)
    val cursor = provider.query(toUri(UriPath(fooEntityName)), Array.empty, null, Array.empty, null)
    cursor.getCount must be (1)
    val head = CursorStream(cursor, EntityTypePersistedInfo(fooEntityType)).head
    fooEntityType.idPkField.getRequired(head) must be (toUriPath(uri2).findId(fooEntityName).get)

    provider.delete(uri1, null, Array.empty) must be (0)
  }
}

class CrudContentProviderForTesting(override val androidApplication: CrudAndroidApplication) extends CrudContentProvider {
  val applicationState = new State
}
