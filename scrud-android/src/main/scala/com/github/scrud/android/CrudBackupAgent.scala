package com.github.scrud.android

import action.ContextWithState
import android.app.backup.{BackupDataOutput, BackupDataInput, BackupAgent}
import com.github.triangle.Logging
import persistence.CursorField._
import android.os.ParcelFileDescriptor
import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import scala.collection.JavaConversions._
import java.util.{Map => JMap,HashMap}
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.{UriPath, EntityType, EntityName, CrudApplication}
import com.github.scrud.util.{CalculatedIterator, Common}
import com.github.scrud.state.State

object CrudBackupAgent {
  private val backupStrategyVersion: Int = 1

  private[scrud] def marshall(map: Map[String,Any]): Array[Byte] = {
    val out = new ByteArrayOutputStream
    try {
      val objectStream = new ObjectOutputStream(out)
      objectStream.writeInt(backupStrategyVersion)
      val jmap: JMap[String,Any] = map
      val hashMap: JMap[String,Any] = new HashMap(jmap)
      objectStream.writeObject(hashMap)
      out.toByteArray
    } finally out.close()
  }

  private[scrud] def unmarshall(bytes: Array[Byte]): Map[String,Any] = {
    val objectStream = new ObjectInputStream(new ByteArrayInputStream(bytes))
    try {
      val strategyVersion = objectStream.readInt()
      if (strategyVersion != backupStrategyVersion) throw new IllegalStateException
      objectStream.readObject().asInstanceOf[JMap[String,Any]].toMap
    } finally objectStream.close()
  }
}

/** A BackupAgent for a CrudApplication.
  * It must be subclassed in order to put it into the AndroidManifest.xml.
  * @author Eric Pabst (epabst@gmail.com)
  */

import CrudBackupAgent._

class CrudBackupAgent(application: CrudApplication) extends BackupAgent with ContextWithState with Logging {
  val crudContext = new AndroidCrudContext(this, application)

  protected lazy val logTag = Common.tryToEvaluate(application.logTag).getOrElse(Common.logTag)

  lazy val applicationState: State = getApplicationContext.asInstanceOf[CrudAndroidApplication]

  final def onBackup(oldState: ParcelFileDescriptor, data: BackupDataOutput, newState: ParcelFileDescriptor) {
    crudContext.withExceptionReporting {
      onBackup(oldState, new BackupTarget {
        def writeEntity(key: String, mapOpt: Option[Map[String,Any]]) {
          debug("Backing up " + key + " <- " + mapOpt)
          mapOpt match {
            case Some(map) =>
              val bytes = marshall(map)
              data.writeEntityHeader(key, bytes.length)
              data.writeEntityData(bytes, bytes.length)
              debug("Backed up " + key + " with " + bytes.length + " bytes")
            case None => data.writeEntityHeader(key, -1)
          }
        }
      }, newState)
    }
  }

  def onBackup(oldState: ParcelFileDescriptor, data: BackupTarget, newState: ParcelFileDescriptor) {
    info("Backing up " + application)
    DeletedEntityIdCrudType.writeEntityRemovals(data, this)
    val crudContext = new AndroidCrudContext(this, application)
    application.allEntityTypes.filter(application.isSavable(_)).foreach { entityType =>
      onBackup(entityType, data, crudContext)
    }
  }

  def onBackup(entityType: EntityType, data: BackupTarget, crudContext: AndroidCrudContext) {
    crudContext.withEntityPersistence[Unit](entityType) { persistence =>
      persistence.findAll(UriPath.EMPTY).foreach { entity =>
        val map = entityType.copyAndUpdate(entity, Map[String,Any]())
        val id = PersistedId.getRequired(entity)
        data.writeEntity(entityType.entityName + "#" + id, Some(map))
      }
    }
  }

  final def onRestore(data: BackupDataInput, appVersionCode: Int, newState: ParcelFileDescriptor) {
    crudContext.withExceptionReporting {
      onRestore(new CalculatedIterator[RestoreItem] {
        def calculateNextValue(): Option[RestoreItem] = {
          if (data.readNextHeader) {
            val key = data.getKey
            val size = data.getDataSize
            val bytes = new Array[Byte](size)
            val actualSize = data.readEntityData(bytes, 0, size)
            debug("Restoring " + key + ": expected " + size + " bytes, read " + actualSize + " bytes")
            if (actualSize != size) throw new IllegalStateException("readEntityData returned " + actualSize + " instead of " + size)
            try {
              val map = unmarshall(bytes)
              debug("Restoring " + key + ": read Map: " + map)
              Some(RestoreItem(key, map))
            } catch {
              case e: Exception =>
                logError("Unable to restore " + key, e)
                //skip this one and do the next
                calculateNextValue()
            }
          } else {
            None
          }
        }
      }, appVersionCode, newState)
    }
  }

  def onRestore(data: Iterator[RestoreItem], appVersionCode: Int, newState: ParcelFileDescriptor) {
    info("Restoring backup of " + application)
    val crudContext = new AndroidCrudContext(this, application)
    val entityTypes = application.allEntityTypes
    data.foreach(restoreItem => {
      val nameOfEntity = restoreItem.key.substring(0, restoreItem.key.lastIndexOf("#"))
      entityTypes.find(_.entityName.name == nameOfEntity).foreach {
        onRestore(_, restoreItem, crudContext)
      }
    })
  }

  def onRestore(entityType: EntityType, restoreItem: RestoreItem, crudContext: AndroidCrudContext) {
    debug("Restoring " + restoreItem.key + " <- " + restoreItem.map)
    val id = restoreItem.key.substring(restoreItem.key.lastIndexOf("#") + 1).toLong
    val writable = entityType.copyAndUpdate(restoreItem.map, crudContext.newWritable(entityType))
    crudContext.withEntityPersistence(entityType) { _.save(Some(id), writable) }
    Unit
  }
}

trait BackupTarget {
  def writeEntity(key: String, map: Option[Map[String,Any]])
}

case class RestoreItem(key: String, map: Map[String,Any])

object DeletedEntityId extends EntityName("DeletedEntityId")

object DeletedEntityIdEntityType extends EntityType(DeletedEntityId) {
  val entityNameField = persisted[String]("entityName")
  val entityIdField = persisted[ID]("entityId")
  // not a val since not used enough to store
  def valueFields = List(entityNameField, entityIdField)
}

/** Helps prevent restoring entities that the user deleted when an onRestore operation happens.
  * It only contains the entityName and ID since it is not intended as a recycle bin,
  * but to delete data in the Backup Service.
  * This entity is in its own CrudApplication by itself, separate from any other CrudApplication.
  * It is intended to be in a separate database owned by the scrud-android framework.
  */
object DeletedEntityIdCrudType extends CrudType(DeletedEntityIdEntityType, SQLitePersistenceFactory) {
  private val application = new CrudApplication(new AndroidPlatformDriver(classOf[res.R])) {
    val name = "scrud.android_deleted"

    val allCrudTypes = List(DeletedEntityIdCrudType)

    val dataVersion = 1
  }

  /** Records that a deletion happened so that it is deleted from the Backup Service.
    * It's ok for this to happen immediately because if a delete is undone,
    * it will be restored independent of this support, and it will then be re-added to the Backup Service later
    * just like any new entity being added.
    */
  def recordDeletion(entityType: EntityType, id: ID, context: ContextWithState) {
    val crudContext = new AndroidCrudContext(context, application)
    val writable = DeletedEntityIdEntityType.copyAndUpdate(
      Map(DeletedEntityIdEntityType.entityNameField.name -> entityType.entityName.name,
        DeletedEntityIdEntityType.entityIdField.name -> id), crudContext.newWritable(entityType))
    crudContext.withEntityPersistence(entityType) { _.save(None, writable) }
  }

  def writeEntityRemovals(data: BackupTarget, context: ContextWithState) {
    val crudContext = new AndroidCrudContext(context, application)
    crudContext.withEntityPersistence(entityType) { persistence =>
      persistence.findAll(UriPath.EMPTY).foreach { entity =>
        val deletedEntityName: String = DeletedEntityIdEntityType.entityNameField.getRequired(entity)
        val deletedId: ID = DeletedEntityIdEntityType.entityIdField.getRequired(entity)
        data.writeEntity(deletedEntityName + "#" + deletedId, None)
      }
    }
  }
}
