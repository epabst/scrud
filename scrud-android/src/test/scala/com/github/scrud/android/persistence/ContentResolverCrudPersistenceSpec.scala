package com.github.scrud.android.persistence

import org.junit.runner.RunWith
import com.github.scrud.util.CrudMockitoSugar
import org.scalatest.matchers.MustMatchers
import org.junit.Test
import com.github.scrud.UriPath
import com.github.scrud.android._
import com.github.scrud.persistence.{DataListener, ListBufferPersistenceFactoryForTesting, ListBufferPersistenceFactory}
import com.github.scrud.EntityName
import scala.Some
import com.github.scrud.android.CrudApplicationForTesting
import org.mockito.Mockito._

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
  val data1 = Map("name" -> Some("George"), "age" -> Some(31), "uri" -> None)
  val data2 = Map("name" -> Some("Wilma"), "age" -> Some(30), "uri" -> None)

  @Test
  def query_mustReturnMultipleRows() {
    val persistence = ContentResolverCrudPersistenceForTesting(fooEntityType, testApplication)
    persistence.saveCopy(None, data1)
    persistence.saveCopy(None, data2)
    val uriPath = UriPath(fooEntityName)
    val results = persistence.findAll(uriPath)
    results.size must be (2)
    results.map(_ - CursorField.idFieldName) must be (List(data2, data1))
  }

  @Test
  def update_mustModifyTheData() {
    val persistence = ContentResolverCrudPersistenceForTesting(fooEntityType, testApplication)
    val id1 = persistence.saveCopy(None, data1)
    persistence.saveCopy(None, data2)
    val data1b = Map("name" -> Some("Greg"), "age" -> Some(32), "uri" -> None, CursorField.idFieldName -> Some(id1))
    persistence.saveCopy(Some(id1), data1b)
    val results = persistence.findAll(UriPath(fooEntityName))
    results.size must be (2)
    results.map(_ - CursorField.idFieldName) must be (List(data2, data1b - CursorField.idFieldName))
  }

  @Test
  def delete_mustDelete() {
    val persistence = ContentResolverCrudPersistenceForTesting(fooEntityType, testApplication)
    val id1 = persistence.saveCopy(None, data1)
    val id2 = persistence.saveCopy(None, data2)
    persistence.delete(UriPath(fooEntityName, id1)) must be (1)
    val results = persistence.findAll(UriPath(fooEntityName))
    results.size must be (1)
    fooEntityType.idPkField.getRequired(results.head) must be (id2)

    persistence.delete(UriPath(fooEntityName, id1)) must be (0)
  }

  @Test
  def listenerMustReceiveNotificationsWhenSaveHappensForDesiredEntityType() {
    val persistenceFactory = ContentResolverPersistenceFactoryForTesting(ListBufferPersistenceFactoryForTesting, testApplication)
    val crudContext = new AndroidCrudContextForTesting(testApplication)

    val listener = mock[DataListener]
    persistenceFactory.listenerSet(fooEntityType, crudContext).addListener(listener)
    val persistence = persistenceFactory.createEntityPersistence(fooEntityType, crudContext)

    // save one that should cause a notification
    persistence.save(None, fooEntityType.copyAndUpdate(data1, persistence.newWritable()))
    verify(listener, times(1)).onChanged()

    // save another that should cause a notification
    persistence.save(None, fooEntityType.copyAndUpdate(data2, persistence.newWritable()))
    verify(listener, times(2)).onChanged()
  }

  @Test
  def listenerMustNotReceiveNotificationsWhenSaveHappensForDifferentEntityType() {
    val persistenceFactory = ContentResolverPersistenceFactoryForTesting(ListBufferPersistenceFactoryForTesting, testApplication)
    val crudContext = new AndroidCrudContextForTesting(testApplication)

    val listener = mock[DataListener]
    persistenceFactory.listenerSet(fooEntityType, crudContext).addListener(listener)

    // save a different EntityType that should not cause a notification
    val persistence = persistenceFactory.createEntityPersistence(barEntityType, crudContext)
    persistence.saveCopy(None, data1)
    verify(listener, never()).onChanged()
  }
}
