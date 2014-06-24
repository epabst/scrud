package com.github.scrud.android.persistence

import org.junit.runner.RunWith
import com.github.scrud.util.CrudMockitoSugar
import org.scalatest.matchers.MustMatchers
import org.junit.Test
import com.github.scrud.{UriPath, EntityName}
import com.github.scrud.android.view.AndroidConversions._
import android.content.{ContentValues, ContentResolver}
import com.github.scrud.android._
import com.github.scrud.persistence.EntityTypeMapForTesting
import scala.Some
import view.AndroidConversions
import android.net.Uri
import com.github.scrud.platform.representation.Persistence
import com.github.scrud.copy.types.MapStorage

/**
 * A behavior specification for [[com.github.scrud.android.persistence.CrudContentProvider]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/18/13
 * Time: 4:59 PM
 */
@RunWith(classOf[CustomRobolectricTestRunner])
class CrudContentProviderSpec extends CrudMockitoSugar with MustMatchers {
  val platformDriver = AndroidPlatformDriverForTesting
  val fooEntityName = EntityName("Foo")
  val fooEntityType = new EntityTypeForTesting(fooEntityName, platformDriver)
  val barEntityName = EntityName("Bar")
  val barEntityType = new EntityTypeForTesting(barEntityName, platformDriver)
  val application = new CrudAndroidApplication(new EntityTypeMapForTesting(fooEntityType, barEntityType))

  private def toUri(uriPath: UriPath): Uri = AndroidConversions.toUri(uriPath, application)

  @Test
  def getType_mustUseLastEntityName() {
    val provider = new CrudContentProviderForTesting(application)
    provider.getType(toUri(fooEntityName.toUri(3))) must be (ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd." + authorityFor(application) + "." + fooEntityName)
    provider.getType(toUri(UriPath(fooEntityName))) must be (ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd." + authorityFor(application) + "." + fooEntityName)
  }

  @Test
  def query_mustReturnMultipleRows() {
    val provider = new CrudContentProviderForTesting(application)
    val commandContext = new AndroidCommandContextForTesting(application)
    val data1 = new MapStorage(fooEntityType.name -> Some("George"), fooEntityType.age -> Some(31), fooEntityType.url -> None)
    provider.insert(toUri(UriPath(fooEntityName)), fooEntityType.copyAndUpdate(MapStorage, data1, fooEntityType.toUri, Persistence.Latest, new ContentValues(), commandContext))
    val data2 = new MapStorage(fooEntityType.name -> Some("Wilma"), fooEntityType.age -> Some(30), fooEntityType.url -> None)
    provider.insert(toUri(UriPath(fooEntityName)), fooEntityType.copyAndUpdate(MapStorage, data2, fooEntityType.toUri, Persistence.Latest, new ContentValues(), commandContext))
    val cursor = provider.query(toUri(UriPath(fooEntityName)), Array.empty, null, Array.empty, null)
    cursor.getCount must be (2)
    CursorStream(cursor, EntityTypePersistedInfo(fooEntityType), commandContext).toList.map(_ - fooEntityType.id) must be (List(data2, data1))
  }

  @Test
  def update_mustModifyTheData() {
    val provider = new CrudContentProviderForTesting(application)
    val commandContext = new AndroidCommandContextForTesting(application)
    val data1 = new MapStorage(fooEntityType.name -> Some("George"), fooEntityType.age -> Some(31), fooEntityType.url -> None)
    val uri1 = provider.insert(toUri(UriPath(fooEntityName)), fooEntityType.copyAndUpdate(MapStorage, data1, fooEntityName.toUri, Persistence.Latest, new ContentValues(), commandContext))
    val id1 = UriPath.findId(toUriPath(uri1), fooEntityName).get
    val data2 = new MapStorage(fooEntityType.name -> Some("Wilma"), fooEntityType.age -> Some(30), fooEntityType.url -> None)
    provider.insert(toUri(UriPath(fooEntityName)), fooEntityType.copyAndUpdate(MapStorage, data2, fooEntityName.toUri, Persistence.Latest, new ContentValues(), commandContext))
    val data1b = new MapStorage(fooEntityType.name -> Some("Greg"), fooEntityType.age -> Some(32), fooEntityType.url -> None, fooEntityType.id -> Some(id1))
    provider.update(uri1, fooEntityType.copyAndUpdate(MapStorage, data1b, fooEntityName.toUri, Persistence.Latest, new ContentValues(), commandContext), null, Array.empty)
    val cursor = provider.query(toUri(UriPath(fooEntityName)), Array.empty, null, Array.empty, null)
    val results = CursorStream(cursor, EntityTypePersistedInfo(fooEntityType), commandContext).toList
    results.size must be (2)
    results.map(_ - fooEntityType.id) must be (List(data2, data1b - fooEntityType.id))
  }

  @Test
  def delete_mustDelete() {
    val provider = new CrudContentProviderForTesting(application)
    val commandContext = new AndroidCommandContextForTesting(application)
    val data1 = new MapStorage(fooEntityType.name -> Some("George"), fooEntityType.age -> Some(31), fooEntityType.url -> None)
    val uri1 = provider.insert(toUri(UriPath(fooEntityName)), fooEntityType.copyAndUpdate(MapStorage, data1, fooEntityName.toUri, Persistence.Latest, new ContentValues(), commandContext))
    val data2 = new MapStorage(fooEntityType.name -> Some("Wilma"), fooEntityType.age -> Some(30), fooEntityType.url -> None)
    val uri2 = provider.insert(toUri(UriPath(fooEntityName)), fooEntityType.copyAndUpdate(MapStorage, data2, fooEntityName.toUri, Persistence.Latest, new ContentValues(), commandContext))
    provider.delete(uri1, null, Array.empty) must be (1)
    val cursor = provider.query(toUri(UriPath(fooEntityName)), Array.empty, null, Array.empty, null)
    cursor.getCount must be (1)
    val head = CursorStream(cursor, EntityTypePersistedInfo(fooEntityType), commandContext).head
    fooEntityType.id.getRequired(Persistence.Latest, head, fooEntityName.toUri, commandContext) must be (toUriPath(uri2).findId(fooEntityName).get)

    provider.delete(uri1, null, Array.empty) must be (0)
  }
}
