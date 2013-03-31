package com.github.scrud.android

import _root_.android.os.ParcelFileDescriptor
import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import com.github.scrud.android.persistence.CursorField.PersistedId
import com.github.scrud._
import platform.TestingPlatformDriver
import scala.collection.mutable
import org.mockito.Mockito._
import org.mockito.Matchers._
import CrudBackupAgent._
import com.github.triangle.BaseField
import org.mockito.stubbing.Answer
import com.github.triangle.PortableField._
import com.github.scrud.state.State
import com.github.scrud.util.{CalculatedIterator, CrudMockitoSugar}
import com.github.scrud.persistence.ListBufferCrudPersistence
import com.github.scrud.EntityName
import scala.Some

/** A test for [[com.github.scrud.android.CrudBackupAgent]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
class CrudBackupAgentSpec extends MustMatchers with CrudMockitoSugar {
  @Test
  def calculatedIteratorShouldWork() {
    val values = List("a", "b", "c").toIterator
    val iterator = new CalculatedIterator[String] {
      def calculateNextValue() = if (values.hasNext) Some(values.next()) else None
    }
    iterator.next() must be ("a")
    iterator.hasNext must be (true)
    iterator.hasNext must be (true)
    iterator.next() must be ("b")
    iterator.hasNext must be (true)
    iterator.next() must be ("c")
    iterator.hasNext must be (false)
    iterator.hasNext must be (false)
  }

  @Test
  def shouldMarshallAndUnmarshall() {
    val map = Map[String,Any]("name" -> "George", "age" -> 35)
    val bytes = marshall(map)
    val copy = unmarshall(bytes)
    copy must be (map)
  }

  @Test
  def shouldSupportBackupAndRestore() {
    val application = mock[CrudApplication]
    val applicationB = mock[CrudApplication]
    val backupTarget = mock[BackupTarget]
    val state1 = mock[ParcelFileDescriptor]
    val state1b = mock[ParcelFileDescriptor]

    val entityType = new EntityTypeForTesting
    val persistence = new EntityPersistenceForTesting(entityType)
    persistence.save(Some(100L), mutable.Map("name" -> Some("Joe"), "age" -> Some(30)))
    persistence.save(Some(101L), mutable.Map("name" -> Some("Mary"), "age" -> Some(28)))
    val entityType2 = new EntityTypeForTesting(EntityName("OtherMap"))
    val persistence2 = new EntityPersistenceForTesting(entityType2)
    persistence2.save(Some(101L), mutable.Map("city" -> Some("Los Angeles"), "state" -> Some("CA")))
    persistence2.save(Some(104L), mutable.Map("city" -> Some("Chicago"), "state" -> Some("IL")))
    val entityTypeB = new EntityTypeForTesting
    val persistenceB = new EntityPersistenceForTesting(entityTypeB)
    val entityType2B = new EntityTypeForTesting(EntityName("OtherMap"))
    val persistence2B = new EntityPersistenceForTesting(entityType2B)
    val state0 = null
    val restoreItems = mutable.ListBuffer[RestoreItem]()
    when(application.allEntityTypes).thenReturn(List[EntityType](entityType, entityType2))
    when(application.isSavable(any[EntityType]())).thenReturn(true)
    when(application.persistenceFactory(entityType)).thenReturn(new PersistenceFactoryForTesting(persistence))
    when(application.persistenceFactory(entityType2)).thenReturn(new PersistenceFactoryForTesting(persistence2))
    when(backupTarget.writeEntity(eql("MyMap#100"), any())).thenAnswer(saveRestoreItem(restoreItems))
    when(backupTarget.writeEntity(eql("MyMap#101"), any())).thenAnswer(saveRestoreItem(restoreItems))
    when(backupTarget.writeEntity(eql("OtherMap#101"), any())).thenAnswer(saveRestoreItem(restoreItems))
    when(backupTarget.writeEntity(eql("OtherMap#104"), any())).thenAnswer(saveRestoreItem(restoreItems))
    when(applicationB.allEntityTypes).thenReturn(List[EntityType](entityTypeB, entityType2B))
    when(applicationB.isSavable(any[EntityType]())).thenReturn(true)
    when(applicationB.persistenceFactory(entityTypeB)).thenReturn(new PersistenceFactoryForTesting(persistenceB))
    when(applicationB.persistenceFactory(entityType2B)).thenReturn(new PersistenceFactoryForTesting(persistence2B))
    val backupAgent = new CrudBackupAgent(application) {
      override lazy val applicationState = new State
    }
    backupAgent.onCreate()
    backupAgent.onBackup(state0, backupTarget, state1)
    backupAgent.onDestroy()

    persistenceB.findAll(UriPath.EMPTY).size must be (0)
    persistence2B.findAll(UriPath.EMPTY).size must be (0)

    val backupAgentB = new CrudBackupAgent(applicationB)
    backupAgentB.onCreate()
    backupAgentB.onRestore(restoreItems.toIterator, 1, state1b)
    backupAgentB.onDestroy()

    val allB = persistenceB.findAll(UriPath.EMPTY)
    allB.size must be (2)
    allB.map(PersistedId.getRequired(_)) must be (List(100L, 101L))

    val all2B = persistence2B.findAll(UriPath.EMPTY)
    all2B.size must be (2)
    all2B.map(PersistedId.getRequired(_)) must be (List(101L, 104L))
  }

  def saveRestoreItem(restoreItems: mutable.ListBuffer[RestoreItem]): Answer[Unit] = answerWithInvocation { invocation =>
    val currentArguments = invocation.getArguments
    currentArguments(1).asInstanceOf[Option[Map[String,Any]]].foreach { map =>
      restoreItems += RestoreItem(currentArguments(0).asInstanceOf[String], map)
    }
  }

  @Test
  def shouldSkipBackupOfGeneratedTypes() {
    val application = mock[CrudApplication]
    val backupTarget = mock[BackupTarget]
    val state1 = mock[ParcelFileDescriptor]
    val entityType = new EntityTypeForTesting
    val persistence = new EntityPersistenceForTesting(entityType)
    val generatedType = new EntityType(EntityName("Generated"), TestingPlatformDriver) {
      val valueFields = List[BaseField](EntityField[EntityTypeForTesting](EntityForTesting), default[Int](100))
    }
    val state0 = null
    when(application.entityNameLayoutPrefixFor(entityType.entityName)).thenReturn("test")
    when(application.entityNameLayoutPrefixFor(generatedType.entityName)).thenReturn("generated")
    when(application.allEntityTypes).thenReturn(List[EntityType](entityType, generatedType))
    when(application.persistenceFactory(any[EntityName]())).thenReturn(new PersistenceFactoryForTesting(persistence))
    //shouldn't call any methods on generatedPersistence
    val backupAgent = new CrudBackupAgent(application) {
      override lazy val applicationState = new State
    }
    backupAgent.onCreate()
    //shouldn't fail even though one is generated
    backupAgent.onBackup(state0, backupTarget, state1)
    backupAgent.onDestroy()
  }
}

class EntityPersistenceForTesting(entityType: EntityType)
    extends ListBufferCrudPersistence(Map.empty[String, Option[Any]], entityType, null)
