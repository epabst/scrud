package com.github.scrud.android

import action.State
import common.CalculatedIterator
import entity.EntityName
import entity.EntityName
import org.junit.Test
import org.junit.runner.RunWith
import com.xtremelabs.robolectric.RobolectricTestRunner
import org.scalatest.matchers.MustMatchers
import android.widget.ListAdapter
import persistence.CursorField.PersistedId
import persistence.EntityType
import scala.collection.mutable
import org.mockito.Mockito._
import org.mockito.Matchers._
import CrudBackupAgent._
import android.os.ParcelFileDescriptor
import common.UriPath
import com.github.triangle.BaseField
import scala.Some
import org.mockito.stubbing.Answer
import com.github.triangle.PortableField._

/** A test for [[com.github.scrud.android.CrudBackupAgent]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[RobolectricTestRunner])
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
    val listAdapter = mock[ListAdapter]
    val backupTarget = mock[BackupTarget]
    val state1 = mock[ParcelFileDescriptor]
    val state1b = mock[ParcelFileDescriptor]

    val persistence = new MyEntityPersistence
    persistence.save(Some(100L), mutable.Map("name" -> "Joe", "age" -> 30))
    persistence.save(Some(101L), mutable.Map("name" -> "Mary", "age" -> 28))
    val persistence2 = new MyEntityPersistence
    persistence2.save(Some(101L), mutable.Map("city" -> "Los Angeles", "state" -> "CA"))
    persistence2.save(Some(104L), mutable.Map("city" -> "Chicago", "state" -> "IL"))
    val entityType = new MyCrudType(persistence)
    val entityType2 = new MyCrudType(new MyEntityType {
      override val entityName = EntityName("OtherMap")
    }, persistence2)
    val persistenceB = new MyEntityPersistence
    val persistence2B = new MyEntityPersistence
    val entityTypeB = new MyCrudType(persistenceB)
    val entityType2B = new MyCrudType(persistence2B) {
      override def entityName = EntityName("OtherMap")
    }
    val state0 = null
    var restoreItems = mutable.ListBuffer[RestoreItem]()
    when(application.allCrudTypes).thenReturn(List[CrudType](entityType, entityType2))
    when(backupTarget.writeEntity(eql("MyMap#100"), any())).thenAnswer(saveRestoreItem(restoreItems))
    when(backupTarget.writeEntity(eql("MyMap#101"), any())).thenAnswer(saveRestoreItem(restoreItems))
    when(backupTarget.writeEntity(eql("OtherMap#101"), any())).thenAnswer(saveRestoreItem(restoreItems))
    when(backupTarget.writeEntity(eql("OtherMap#104"), any())).thenAnswer(saveRestoreItem(restoreItems))
    when(applicationB.allCrudTypes).thenReturn(List[CrudType](entityTypeB, entityType2B))
    val backupAgent = new CrudBackupAgent(application) {
      override val applicationState = new State {}
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
    val listAdapter = mock[ListAdapter]
    val backupTarget = mock[BackupTarget]
    val state1 = mock[ParcelFileDescriptor]
    val persistenceFactory = mock[GeneratedPersistenceFactory[Map[String, Any]]]
    val persistence = new MyEntityPersistence
    val entityType = new MyCrudType(persistence)
    val generatedType = new CrudType(new EntityType(EntityName("Generated")) {
      def valueFields = List[BaseField](ParentField(MyEntityType), default[Int](100))
    }, persistenceFactory) with StubCrudType
    val state0 = null
    when(application.allCrudTypes).thenReturn(List[CrudType](entityType, generatedType))
    //shouldn't call any methods on generatedPersistence
    val backupAgent = new CrudBackupAgent(application) {
      override val applicationState = new State {}
    }
    backupAgent.onCreate()
    //shouldn't fail even though one is generated
    backupAgent.onBackup(state0, backupTarget, state1)
    backupAgent.onDestroy()
  }
}

class MyEntityPersistence extends ListBufferCrudPersistence(Map.empty[String, Any], null, null)
