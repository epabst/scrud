package com.github.scrud.android.backup

import android.app.backup.{BackupDataOutput, BackupDataInput, BackupAgent}
import android.os.ParcelFileDescriptor
import java.io.{ObjectInputStream, ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import scala.collection.JavaConversions._
import java.util.{Map => JMap}
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.EntityType
import com.github.scrud.util.{ExternalLogging, DelegateLogging}
import com.github.scrud.state.State
import scala.Some
import com.github.scrud.android.AndroidCommandContext
import com.github.scrud.android.state.ActivityStateHolder
import com.github.scrud.android.CrudAndroidApplication
import com.github.scrud.copy.types.MapStorage
import com.github.scrud.copy.CopyContext

/** A BackupAgent for a CrudApplication.
  * It must be subclassed in order to put it into the AndroidManifest.xml.
  * @author Eric Pabst (epabst@gmail.com)
  */

class CrudBackupAgent extends BackupAgent with ActivityStateHolder with DelegateLogging {
  lazy val androidApplication = getApplicationContext.asInstanceOf[CrudAndroidApplication]

  val commandContext = new AndroidCommandContext(this, androidApplication)
  val commandContextWithBackupApplication: AndroidCommandContext = commandContext.copy(entityTypeMap = androidApplication.deletedEntityTypeMap)

  override protected def loggingDelegate: ExternalLogging = androidApplication.applicationName

  lazy val activityState: State = new State
  lazy val applicationState: State = {
    androidApplication.applicationState
  }

  final def onBackup(oldState: ParcelFileDescriptor, data: BackupDataOutput, newState: ParcelFileDescriptor) {
    commandContext.withExceptionReporting {
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
    info("Backing up " + androidApplication.applicationName)
    writeEntityRemovals(data)
    androidApplication.entityTypeMap.allEntityTypes.filter(androidApplication.entityTypeMap.isSavable(_)).foreach { entityType =>
      onBackup(entityType, data, commandContext)
    }
  }

  def onBackup(entityType: EntityType, data: BackupTarget, commandContext: AndroidCommandContext) {
    commandContext.findAll(entityType.toUri, MapStorage).foreach { entityStorage =>
      val id = entityStorage.get(entityType.id).get
      data.writeEntity(entityType.entityName + "#" + id, Some(entityStorage.toMap))
    }
  }

  private def writeEntityRemovals(data: BackupTarget) {
    val deletedEntityIdEntityType = androidApplication.deletedEntityTypeMap.deletedEntityIdEntityType
    val persistence = commandContextWithBackupApplication.persistenceFor(deletedEntityIdEntityType)
    val uri = deletedEntityIdEntityType.toUri
    val copyContext = new CopyContext(uri, commandContextWithBackupApplication)
    val nameSourceField = deletedEntityIdEntityType.entityNameField.toAdaptableField.sourceFieldOrFail(persistence.sourceType)
    val idSourceField = deletedEntityIdEntityType.entityIdField.toAdaptableField.sourceFieldOrFail(persistence.sourceType)
    persistence.findAll(uri).foreach { entity =>
      val deletedEntityName: String = nameSourceField.findValue(entity, copyContext).get
      val deletedId: ID = idSourceField.findValue(entity, copyContext).get
      data.writeEntity(deletedEntityName + "#" + deletedId, None)
    }
  }

  final def onRestore(data: BackupDataInput, appVersionCode: Int, newState: ParcelFileDescriptor) {
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
    commandContext.withExceptionReporting {
      val stream = Stream.continually(calculateNextValue()).takeWhile(_.isDefined).flatten
      onRestore(stream, appVersionCode, newState)
    }
  }

  def onRestore(data: Stream[RestoreItem], appVersionCode: Int, newState: ParcelFileDescriptor) {
    info("Restoring backup of " + androidApplication.applicationName)
    val commandContext = new AndroidCommandContext(this, androidApplication)
    val entityTypes = androidApplication.entityTypeMap.allEntityTypes
    data.foreach { restoreItem =>
      val nameOfEntity = restoreItem.key.substring(0, restoreItem.key.lastIndexOf("#"))
      entityTypes.find(_.entityName.name == nameOfEntity).foreach {
        onRestore(_, restoreItem, commandContext)
      }
    }
  }

  def onRestore(entityType: EntityType, restoreItem: RestoreItem, commandContext: AndroidCommandContext) {
    debug("Restoring " + restoreItem.key + " <- " + restoreItem.map)
    val id = restoreItem.key.substring(restoreItem.key.lastIndexOf("#") + 1).toLong
    commandContext.save(entityType.entityName, MapStorage, Some(id), new MapStorage(entityType.entityName, restoreItem.map))
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
