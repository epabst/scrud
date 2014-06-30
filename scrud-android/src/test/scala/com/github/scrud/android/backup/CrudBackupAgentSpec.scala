package com.github.scrud.android.backup

import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import com.github.scrud.util.CrudMockitoSugar
import org.junit.Test
import com.github.scrud.android.backup.CrudBackupAgent._
import _root_.android.os.ParcelFileDescriptor
import scala.collection.mutable
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.github.scrud.persistence._
import com.github.scrud.platform.TestingPlatformDriver
import com.github.scrud._
import org.mockito.stubbing.Answer
import com.github.scrud.android._
import com.github.scrud.copy.types.{Default, Validation, MapStorage}
import com.github.scrud.types.{NaturalIntQT, TitleQT}
import com.github.scrud.platform.representation.{SelectUI, EditUI, Persistence}
import com.github.scrud.android.EntityTypeForTesting
import com.github.scrud.EntityName
import org.robolectric.annotation.Config

/** A test for [[com.github.scrud.android.backup.CrudBackupAgent]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
@Config(manifest = "target/generated/AndroidManifest.xml")
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
    val restoreItems = mutable.ListBuffer[RestoreItem]()

    // Backup
    {
      val entityTypeA = new EntityTypeForTesting()
      val entityTypeB = new EntityTypeForTesting(EntityName("OtherMap")) {
        val city = field("city", TitleQT, Seq(Persistence(1), EditUI, SelectUI, Validation.requiredString, LoadingIndicator("...")))
        val state = field("state", TitleQT, Seq(Persistence(1), EditUI, SelectUI, Validation.requiredString, LoadingIndicator("...")))
      }
      val application1 = new CrudAndroidApplication(new EntityTypeMapForTesting(
        entityTypeA -> PersistenceFactoryForTesting,
        entityTypeB -> PersistenceFactoryForTesting))

      val commandContext = new AndroidCommandContextForTesting(application1)
      commandContext.save(entityTypeA.entityName, Some(100L), MapStorage, new MapStorage(entityTypeA.name -> Some("Joe"), entityTypeA.age -> Some(30)))
      commandContext.save(entityTypeA.entityName, Some(101L), MapStorage, new MapStorage(entityTypeA.name -> Some("Mary"), entityTypeA.age -> Some(28)))
      commandContext.save(entityTypeB.entityName, Some(101L), MapStorage, new MapStorage(entityTypeB.city -> Some("Los Angeles"), entityTypeB.state -> Some("CA")))
      commandContext.save(entityTypeB.entityName, Some(104L), MapStorage, new MapStorage(entityTypeB.city -> Some("Chicago"), entityTypeB.state -> Some("IL")))
      val state0 = null

      val backupTarget = mock[BackupTarget]
      when(backupTarget.writeEntity(eql("MyMap#100"), any())).thenAnswer(saveRestoreItem(restoreItems))
      when(backupTarget.writeEntity(eql("MyMap#101"), any())).thenAnswer(saveRestoreItem(restoreItems))
      when(backupTarget.writeEntity(eql("OtherMap#101"), any())).thenAnswer(saveRestoreItem(restoreItems))
      when(backupTarget.writeEntity(eql("OtherMap#104"), any())).thenAnswer(saveRestoreItem(restoreItems))
      val backupAgent1 = new CrudBackupAgent {
        override lazy val androidApplication: CrudAndroidApplicationLike = application1
      }
      val state1 = mock[ParcelFileDescriptor]
      backupAgent1.onCreate()
      backupAgent1.onBackup(state0, backupTarget, state1)
      backupAgent1.onDestroy()
    }

    // Restore
    {
      val entityTypeA2 = new EntityTypeForTesting()
      val entityTypeB2 = new EntityTypeForTesting(EntityName("OtherMap"))
      val application2 = new CrudAndroidApplication(new EntityTypeMapForTesting(
        entityTypeA2 -> PersistenceFactoryForTesting,
        entityTypeB2 -> PersistenceFactoryForTesting))
      val commandContext = new AndroidCommandContextForTesting(application2)

      commandContext.findAll(entityTypeA2.toUri, entityTypeA2.id).size must be (0)
      commandContext.findAll(entityTypeB2.toUri, entityTypeB2.id).size must be (0)

      val backupAgent2 = new CrudBackupAgent {
        override lazy val androidApplication: CrudAndroidApplicationLike = application2
      }
      val state2 = mock[ParcelFileDescriptor]
      backupAgent2.onCreate()
      backupAgent2.onRestore(restoreItems.toStream, 1, state2)
      backupAgent2.onDestroy()

      val allA = commandContext.findAll(entityTypeA2.toUri, entityTypeA2.id)
      allA must be(List(100L, 101L))

      val allB = commandContext.findAll(entityTypeB2.toUri, entityTypeB2.id)
      allB must be(List(101L, 104L))
    }
  }

  def saveRestoreItem(restoreItems: mutable.ListBuffer[RestoreItem]): Answer[Unit] = answerWithInvocation { invocation =>
    val currentArguments = invocation.getArguments
    currentArguments(1).asInstanceOf[Option[Map[String,Any]]].foreach { map =>
      restoreItems += RestoreItem(currentArguments(0).asInstanceOf[String], map)
    }
  }

  @Test
  def shouldSkipBackupOfGeneratedTypes() {
    val entityType = new EntityTypeForTesting
    val generatedType = new EntityType(EntityName("Generated"), TestingPlatformDriver) {
      val number = field("number", NaturalIntQT, Seq(Default(100)))
    }
    val state0 = null
    val application = new CrudAndroidApplication(new EntityTypeMapForTesting(
      entityType -> PersistenceFactoryForTesting,
      generatedType -> new DerivedPersistenceFactory[MapStorage]() {
        override def findAll(entityType: EntityType, uri: UriPath, persistenceConnection: PersistenceConnection): Seq[MapStorage] = {
          throw new IllegalStateException("should not be called")
        }
      }))
    //shouldn't call any methods on generatedPersistence
    val backupAgent = new CrudBackupAgent {
      override lazy val androidApplication = application
    }

    val backupTarget = mock[BackupTarget]
    val state1 = mock[ParcelFileDescriptor]

    backupAgent.onCreate()
    //shouldn't fail even though one is generated
    backupAgent.onBackup(state0, backupTarget, state1)
    backupAgent.onDestroy()
  }
}
