package com.github.scrud.android.backup

import android.app.backup.{BackupDataOutput, BackupDataInput, BackupAgent}
import com.github.triangle.Logging
import android.os.ParcelFileDescriptor
import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import scala.collection.JavaConversions._
import java.util.{Map => JMap}
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.{UriPath, EntityType, CrudApplication}
import com.github.scrud.util.{CalculatedIterator, Common}
import com.github.scrud.state.State
import scala.Some
import com.github.scrud.android.AndroidCrudContext
import com.github.scrud.android.state.ActivityStateHolder
import com.github.scrud.android.CrudAndroidApplication
import com.github.scrud.android.persistence.CursorField.PersistedId

/** A BackupAgent for a CrudApplication.
  * It must be subclassed in order to put it into the AndroidManifest.xml.
  * @author Eric Pabst (epabst@gmail.com)
  */

class CrudBackupAgent(application: CrudApplication) extends BackupAgent with ActivityStateHolder with Logging {
  val crudContext = new AndroidCrudContext(this, application)
  val crudContextWithBackupApplication = crudContext.copy(application = DeletedEntityIdApplication)

  protected lazy val logTag = Common.tryToEvaluate(application.logTag).getOrElse(Common.logTag)

  lazy val activityState: State = new State
  lazy val applicationState: State = getApplicationContext.asInstanceOf[CrudAndroidApplication].applicationState

  final def onBackup(oldState: ParcelFileDescriptor, data: BackupDataOutput, newState: ParcelFileDescriptor) {
    crudContext.withExceptionReporting {
      onBackup(oldState, new BackupTarget {
        def writeEntity(key: String, mapOpt: Option[Map[String,Any]]) {
          debug("Backing up " + key + " <- " + mapOpt)
          mapOpt match {
            case Some(map) =>
              val bytes = CrudBackupAgent.marshall(map)
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
    writeEntityRemovals(data)
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

  private def writeEntityRemovals(data: BackupTarget) {
    import DeletedEntityIdApplication.deletedEntityIdEntityType
    crudContextWithBackupApplication.withEntityPersistence(deletedEntityIdEntityType) { persistence =>
      persistence.findAll(UriPath.EMPTY).foreach { entity =>
        val deletedEntityName: String = deletedEntityIdEntityType.entityNameField.getRequired(entity)
        val deletedId: ID = deletedEntityIdEntityType.entityIdField.getRequired(entity)
        data.writeEntity(deletedEntityName + "#" + deletedId, None)
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
              val map = CrudBackupAgent.unmarshall(bytes)
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

object CrudBackupAgent {
  private val backupStrategyVersion: Int = 1

  private[scrud] def marshall(map: Map[String,Any]): Array[Byte] = {
    val out = new ByteArrayOutputStream
    try {
      val objectStream = new ObjectOutputStream(out)
      objectStream.writeInt(backupStrategyVersion)
      val jmap: JMap[String,Any] = map
      val hashMap: JMap[String,Any] = new java.util.HashMap[String,Any](jmap)
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
