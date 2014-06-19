package com.github.scrud.android.backup

import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import com.github.scrud.util.CrudMockitoSugar
import org.junit.Test
import com.github.scrud.android.backup.CrudBackupAgent._
import _root_.android.os.ParcelFileDescriptor
import scala.collection.mutable
import com.github.scrud._
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.github.scrud.state.State
import com.github.scrud.persistence.{EntityTypeMapForTesting, PersistenceFactoryForTesting, EntityPersistenceForTesting}
import com.github.scrud.platform.TestingPlatformDriver
import com.github.scrud.EntityName
import scala.Some
import org.mockito.stubbing.Answer
import com.github.scrud.android.{CrudAndroidApplication, CustomRobolectricTestRunner}

/** A test for [[com.github.scrud.android.backup.CrudBackupAgent]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
class CrudBackupAgentSpec extends MustMatchers with CrudMockitoSugar {
  @Test
  def shouldMarshallAndUnmarshall() {
    val map = Map[String,Any]("name" -> "George", "age" -> 35)
    val bytes = marshall(map)
    val copy = unmarshall(bytes)
    copy must be (map)
  }

  @Test
  def shouldSupportBackupAndRestore() {
    val backupTarget = mock[BackupTarget]
    val state1 = mock[ParcelFileDescriptor]
    val state1b = mock[ParcelFileDescriptor]

    val entityType = new EntityTypeForTesting()
    val persistence = new EntityPersistenceForTesting(entityType)
    persistence.save(Some(100L), mutable.Map("name" -> Some("Joe"), "age" -> Some(30)))
    persistence.save(Some(101L), mutable.Map("name" -> Some("Mary"), "age" -> Some(28)))
    val entityType2 = new EntityTypeForTesting(EntityName("OtherMap"))
    val persistence2 = new EntityPersistenceForTesting(entityType2)
    persistence2.save(Some(101L), mutable.Map("city" -> Some("Los Angeles"), "state" -> Some("CA")))
    persistence2.save(Some(104L), mutable.Map("city" -> Some("Chicago"), "state" -> Some("IL")))
    val entityTypeB = new EntityTypeForTesting()
    val persistenceB = new EntityPersistenceForTesting(entityTypeB)
    val entityType2B = new EntityTypeForTesting(EntityName("OtherMap"))
    val persistence2B = new EntityPersistenceForTesting(entityType2B)
    val state0 = null
    val restoreItems = mutable.ListBuffer[RestoreItem]()
    val application = new CrudAndroidApplication(new EntityNavigation(new EntityTypeMapForTesting(
      entityType -> new PersistenceFactoryForTesting(persistence),
      entityType2 -> new PersistenceFactoryForTesting(persistence2))))
    when(backupTarget.writeEntity(eql("MyMap#100"), any())).thenAnswer(saveRestoreItem(restoreItems))
    when(backupTarget.writeEntity(eql("MyMap#101"), any())).thenAnswer(saveRestoreItem(restoreItems))
    when(backupTarget.writeEntity(eql("OtherMap#101"), any())).thenAnswer(saveRestoreItem(restoreItems))
    when(backupTarget.writeEntity(eql("OtherMap#104"), any())).thenAnswer(saveRestoreItem(restoreItems))
    val applicationB = new CrudAndroidApplication(new EntityNavigation(new EntityTypeMapForTesting(
      entityTypeB -> new PersistenceFactoryForTesting(persistenceB),
      entityType2B -> new PersistenceFactoryForTesting(persistence2B))))
    val backupAgent = new CrudBackupAgent {
      override lazy val applicationState = new State
    }
    backupAgent.onCreate()
    backupAgent.onBackup(state0, backupTarget, state1)
    backupAgent.onDestroy()

    persistenceB.findAll(UriPath.EMPTY).size must be (0)
    persistence2B.findAll(UriPath.EMPTY).size must be (0)

    val backupAgentB = new CrudBackupAgent {
      override lazy val applicationState = new State
    }
    backupAgentB.onCreate()
    backupAgentB.onRestore(restoreItems.toIterator, 1, state1b)
    backupAgentB.onDestroy()

    val allB = persistenceB.findAll(UriPath.EMPTY, entityTypeB.id, )
    allB must be (List(100L, 101L))

    val all2B = persistence2B.findAll(UriPath.EMPTY, entityType2B.id, )
    all2B must be (List(101L, 104L))
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
      val valueFields = List[BaseField](EntityField[EntityTypeForTesting](EntityForTesting), PortableField.default[Int](100))
    }
    val state0 = null
    when(application.allEntityTypes).thenReturn(List[EntityType](entityType, generatedType))
    when(application.persistenceFactory(any[EntityName]())).thenReturn(new PersistenceFactoryForTesting(persistence))
    //shouldn't call any methods on generatedPersistence
    val backupAgent = new CrudBackupAgent {
      override lazy val applicationState = new State
    }
    backupAgent.onCreate()
    //shouldn't fail even though one is generated
    backupAgent.onBackup(state0, backupTarget, state1)
    backupAgent.onDestroy()
  }
}
