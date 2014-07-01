package com.github.scrud.android.persistence

import org.junit.runner.RunWith
import org.junit.Test
import com.github.scrud.UriPath
import com.github.scrud.android._
import com.github.scrud.persistence.DataListener
import org.mockito.Mockito._
import com.github.scrud.copy.types.MapStorage
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

/**
 * A behavior specification for [[com.github.scrud.android.persistence.ContentResolverCrudPersistence]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 3/18/13
 * Time: 4:59 PM
 */
@RunWith(classOf[CustomRobolectricTestRunner])
@Config(manifest = "target/generated/AndroidManifest.xml")
class ContentResolverCrudPersistenceSpec extends ScrudRobolectricSpec {
  val platformDriver = AndroidPlatformDriverForTesting
  val entityType1 = EntityTypeForTesting
  val entityType2 = new EntityTypeForTesting(EntityForTesting2)
  val data1 = new MapStorage(entityType1.name -> Some("George"), entityType1.age -> Some(31), entityType1.url -> None)
  val data2 = new MapStorage(entityType1.name -> Some("Wilma"), entityType1.age -> Some(30), entityType1.url -> None)

  @Test
  def query_mustReturnMultipleRows() {
    val commandContext = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).get().commandContext
    val persistence = new ContentResolverCrudPersistenceForTesting(entityType1, commandContext)
    persistence.save(None, MapStorage, data1, commandContext)
    persistence.save(None, MapStorage, data2, commandContext)
    val results = persistence.findAll(EntityForTesting.toUri)
    results.size must be (2)
    results.map(_ - entityType1.id) must be (List(data2, data1))
  }

  @Test
  def findAll_mustOnlyReturnRowsWithMatchingId() {
    val commandContext = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).get().commandContext
    val persistence = new ContentResolverCrudPersistenceForTesting(entityType1, commandContext.androidApplication, commandContext)
    val id1 = persistence.save(None, MapStorage, data1, commandContext)
    persistence.save(None, MapStorage, data2, commandContext)
    val uriPath = UriPath(EntityForTesting, id1)
    val results = persistence.findAll(uriPath)
    results.size must be (1)
    results.map(_ - entityType1.id) must be (List(data1))
  }

  @Test
  def findAll_mustOnlyReturnRowsWithMatchingId_otherEntityLaterInPath() {
    val commandContext = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).get().commandContext
    val persistence = new ContentResolverCrudPersistenceForTesting(entityType1, commandContext)
    val id1 = persistence.save(None, MapStorage, data1, commandContext)
    persistence.save(None, MapStorage, data2, commandContext)
    val uriPath = UriPath(EntityForTesting, id1) / EntityForTesting2
    val results = persistence.findAll(uriPath)
    results.size must be (1)
    results.map(_ - entityType1.id) must be (List(data1))
  }

  @Test
  def update_mustModifyTheData() {
    val commandContext = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).get().commandContext
    val persistence = new ContentResolverCrudPersistenceForTesting(entityType1, commandContext)
    val id1 = persistence.save(None, MapStorage, data1, commandContext)
    persistence.save(None, MapStorage, data2, commandContext)
    val data1b = new MapStorage(entityType1.name -> Some("Greg"), entityType1.age -> Some(32), entityType1.url -> None, entityType1.id -> Some(id1))
    persistence.save(Some(id1), MapStorage, data1b, commandContext)
    val results = persistence.findAll(UriPath(EntityForTesting))
    results.size must be (2)
    results.map(_ - entityType1.id) must be (List(data2, data1b - entityType1.id))
  }

  @Test
  def delete_mustDelete() {
    val commandContext = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).get().commandContext
    val persistence = new ContentResolverCrudPersistenceForTesting(entityType1, commandContext)
    val id1 = persistence.save(None, MapStorage, data1, commandContext)
    val id2 = persistence.save(None, MapStorage, data2, commandContext)
    persistence.delete(UriPath(EntityForTesting, id1)) must be (1)
    val resultIds = persistence.findAll(UriPath(EntityForTesting), entityType1.id, commandContext)
    resultIds must be (Seq(id2))

    persistence.delete(UriPath(EntityForTesting, id1)) must be (0)
  }

  @Test
  def listenerMustReceiveNotificationsWhenSaveHappensForDesiredEntityType() {
    val persistenceFactory = new ContentResolverPersistenceFactory(platformDriver.localDatabasePersistenceFactory)
    val commandContext = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).get().commandContext
    val listener = mock[DataListener]
    persistenceFactory.listenerSet(entityType1, commandContext.sharedContext).addListener(listener)
    val persistence = persistenceFactory.createEntityPersistence(entityType1, commandContext.persistenceConnection)

    // save one that should cause a notification
    persistence.save(None, MapStorage, data1, commandContext)
    verify(listener, times(1)).onChanged()

    // save another that should cause a notification
    persistence.save(None, MapStorage, data2, commandContext)
    verify(listener, times(2)).onChanged()
  }

  @Test
  def listenerMustNotReceiveNotificationsWhenSaveHappensForDifferentEntityType() {
    val persistenceFactory = new ContentResolverPersistenceFactory(platformDriver.localDatabasePersistenceFactory)
    val commandContext = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).get().commandContext

    val listener = mock[DataListener]
    persistenceFactory.listenerSet(entityType1, commandContext.sharedContext).addListener(listener)

    // save a different EntityType that should not cause a notification
    val barPersistence = persistenceFactory.createEntityPersistence(entityType2, commandContext.persistenceConnection)
    barPersistence.save(None, MapStorage, data1, commandContext)
    verify(listener, never()).onChanged()
  }
}
