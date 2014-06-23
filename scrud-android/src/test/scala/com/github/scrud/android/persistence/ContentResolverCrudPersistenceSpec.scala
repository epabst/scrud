package com.github.scrud.android.persistence

import org.junit.runner.RunWith
import com.github.scrud.util.CrudMockitoSugar
import org.scalatest.matchers.MustMatchers
import org.junit.Test
import com.github.scrud.{EntityNavigation, UriPath, EntityName}
import com.github.scrud.android._
import com.github.scrud.persistence.{EntityTypeMapForTesting, DataListener, ListBufferPersistenceFactoryForTesting}
import scala.Some
import org.mockito.Mockito._
import com.github.scrud.copy.types.MapStorage

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
  val barEntityName = EntityName("Bar")
  val barEntityType = new EntityTypeForTesting(barEntityName, platformDriver)
  val testApplication = new CrudAndroidApplication(new EntityTypeMapForTesting(fooEntityType, barEntityType))
  val data1 = new MapStorage(fooEntityType.name -> Some("George"), fooEntityType.age -> Some(31), fooEntityType.url -> None)
  val data2 = new MapStorage(fooEntityType.name -> Some("Wilma"), fooEntityType.age -> Some(30), fooEntityType.url -> None)

  @Test
  def query_mustReturnMultipleRows() {
    val commandContext = new AndroidCommandContextForTesting(testApplication)
    val persistence = new ContentResolverCrudPersistenceForTesting(fooEntityType, testApplication, commandContext)
    persistence.save(None, MapStorage, data1, commandContext)
    persistence.save(None, MapStorage, data2, commandContext)
    val results = persistence.findAll(fooEntityName.toUri)
    results.size must be (2)
    results.map(_ - fooEntityType.id) must be (List(data2, data1))
  }

  @Test
  def findAll_mustOnlyReturnRowsWithMatchingId() {
    val commandContext = new AndroidCommandContextForTesting(testApplication)
    val persistence = new ContentResolverCrudPersistenceForTesting(fooEntityType, testApplication, commandContext)
    val id1 = persistence.save(None, MapStorage, data1, commandContext)
    persistence.save(None, MapStorage, data2, commandContext)
    val uriPath = UriPath(fooEntityName, id1)
    val results = persistence.findAll(uriPath)
    results.size must be (1)
    results.map(_ - fooEntityType.id) must be (List(data1))
  }

  @Test
  def findAll_mustOnlyReturnRowsWithMatchingId_otherEntityLaterInPath() {
    val commandContext = new AndroidCommandContextForTesting(testApplication)
    val persistence = new ContentResolverCrudPersistenceForTesting(fooEntityType, testApplication, commandContext)
    val id1 = persistence.save(None, MapStorage, data1, commandContext)
    persistence.save(None, MapStorage, data2, commandContext)
    val uriPath = UriPath(fooEntityName, id1) / barEntityName
    val results = persistence.findAll(uriPath)
    results.size must be (1)
    results.map(_ - fooEntityType.id) must be (List(data1))
  }

  @Test
  def update_mustModifyTheData() {
    val commandContext = new AndroidCommandContextForTesting(testApplication)
    val persistence = new ContentResolverCrudPersistenceForTesting(fooEntityType, testApplication, commandContext)
    val id1 = persistence.save(None, MapStorage, data1, commandContext)
    persistence.save(None, MapStorage, data2, commandContext)
    val data1b = new MapStorage(fooEntityType.name -> Some("Greg"), fooEntityType.age -> Some(32), fooEntityType.url -> None, fooEntityType.id -> Some(id1))
    persistence.save(Some(id1), MapStorage, data1b, commandContext)
    val results = persistence.findAll(UriPath(fooEntityName))
    results.size must be (2)
    results.map(_ - fooEntityType.id) must be (List(data2, data1b - fooEntityType.id))
  }

  @Test
  def delete_mustDelete() {
    val commandContext = new AndroidCommandContextForTesting(testApplication)
    val persistence = new ContentResolverCrudPersistenceForTesting(fooEntityType, testApplication, commandContext)
    val id1 = persistence.save(None, MapStorage, data1, commandContext)
    val id2 = persistence.save(None, MapStorage, data2, commandContext)
    persistence.delete(UriPath(fooEntityName, id1)) must be (1)
    val resultIds = persistence.findAll(UriPath(fooEntityName), fooEntityType.id, commandContext)
    resultIds must be (Seq(id2))

    persistence.delete(UriPath(fooEntityName, id1)) must be (0)
  }

  @Test
  def listenerMustReceiveNotificationsWhenSaveHappensForDesiredEntityType() {
    val persistenceFactory = new ContentResolverPersistenceFactoryForTesting(ListBufferPersistenceFactoryForTesting, testApplication)
    val commandContext = new AndroidCommandContextForTesting(testApplication)
    val listener = mock[DataListener]
    persistenceFactory.listenerSet(fooEntityType, commandContext.sharedContext).addListener(listener)
    val persistence = persistenceFactory.createEntityPersistence(fooEntityType, commandContext.persistenceConnection)

    // save one that should cause a notification
    persistence.save(None, MapStorage, data1, commandContext)
    verify(listener, times(1)).onChanged()

    // save another that should cause a notification
    persistence.save(None, MapStorage, data2, commandContext)
    verify(listener, times(2)).onChanged()
  }

  @Test
  def listenerMustNotReceiveNotificationsWhenSaveHappensForDifferentEntityType() {
    val persistenceFactory = new ContentResolverPersistenceFactoryForTesting(ListBufferPersistenceFactoryForTesting, testApplication)
    val commandContext = new AndroidCommandContextForTesting(testApplication)

    val listener = mock[DataListener]
    persistenceFactory.listenerSet(fooEntityType, commandContext.sharedContext).addListener(listener)

    // save a different EntityType that should not cause a notification
    val barPersistence = persistenceFactory.createEntityPersistence(barEntityType, commandContext.persistenceConnection)
    barPersistence.save(None, MapStorage, data1, commandContext)
    verify(listener, never()).onChanged()
  }
}
