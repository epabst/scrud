package com.github.scrud.android.persistence

import org.junit.runner.RunWith
import com.github.scrud.util.CrudMockitoSugar
import org.scalatest.matchers.MustMatchers
import org.junit.Test
import com.github.scrud.UriPath
import com.github.scrud.android._
import com.github.scrud.persistence.ListBufferPersistenceFactory
import com.github.scrud.EntityName
import scala.Some
import com.github.scrud.android.CrudApplicationForTesting

/**
 * A behavior specification for [[com.github.scrud.android.persistence.ContentResolverCrudPersistence]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/18/13
 * Time: 4:59 PM
 */
@RunWith(classOf[CustomRobolectricTestRunner])
class ContentResolverCrudPersistenceSpec extends CrudMockitoSugar with MustMatchers {
  val platformDriver = new AndroidPlatformDriver(null)
  val fooEntityName = EntityName("Foo")
  val fooEntityType = new EntityTypeForTesting(fooEntityName, platformDriver)
  val fooCrudType = new CrudTypeForTesting(fooEntityType, new ListBufferPersistenceFactory[Map[String,Option[Any]]](Map.empty))
  val barEntityName = EntityName("Bar")
  val barEntityType = new EntityTypeForTesting(barEntityName, platformDriver)
  val barCrudType = new CrudTypeForTesting(barEntityType, new ListBufferPersistenceFactory[Map[String,Option[Any]]](Map.empty))
  val testApplication = new CrudApplicationForTesting(fooCrudType, barCrudType)

  @Test
  def query_mustReturnMultipleRows() {
    val persistence = ContentResolverCrudPersistenceForTesting(testApplication)
    val data1 = Map("name" -> Some("George"), "age" -> Some(31), "uri" -> None)
    persistence.save(None, fooEntityType.copyAndUpdate(data1, persistence.newWritable()))
    val data2 = Map("name" -> Some("Wilma"), "age" -> Some(30), "uri" -> None)
    persistence.save(None, fooEntityType.copyAndUpdate(data2, persistence.newWritable()))
    val uriPath = UriPath(fooEntityName)
    val results = persistence.findAll(uriPath)
    results.size must be (2)
    results.map(_ - "_id") must be (List(data2, data1))
  }

  @Test
  def update_mustModifyTheData() {
    val persistence = ContentResolverCrudPersistenceForTesting(testApplication)
    val data1 = Map("name" -> Some("George"), "age" -> Some(31), "uri" -> None)
    val id1 = persistence.save(None, fooEntityType.copyAndUpdate(data1, persistence.newWritable()))
    val data2 = Map("name" -> Some("Wilma"), "age" -> Some(30), "uri" -> None)
    persistence.save(None, fooEntityType.copyAndUpdate(data2, persistence.newWritable()))
    val data1b = Map("name" -> Some("Greg"), "age" -> Some(32), "uri" -> None)
    persistence.save(Some(id1), fooEntityType.copyAndUpdate(data1b, persistence.newWritable()))
    val results = persistence.findAll(UriPath(fooEntityName))
    results.size must be (2)
    results.map(_ - "_id") must be (List(data2, data1b))
  }

  @Test
  def delete_mustDelete() {
    val persistence = ContentResolverCrudPersistenceForTesting(testApplication)
    val data1 = Map("name" -> Some("George"), "age" -> Some(31), "uri" -> None)
    val id1 = persistence.save(None, fooEntityType.copyAndUpdate(data1, persistence.newWritable()))
    val data2 = Map("name" -> Some("Wilma"), "age" -> Some(30), "uri" -> None)
    val id2 = persistence.save(None, fooEntityType.copyAndUpdate(data2, persistence.newWritable()))
    persistence.delete(UriPath(fooEntityName, id1)) must be (1)
    val results = persistence.findAll(UriPath(fooEntityName))
    results.size must be (1)
    fooEntityType.idPkField.getRequired(results.head) must be (id2)

    persistence.delete(UriPath(fooEntityName, id1)) must be (0)
  }
}
