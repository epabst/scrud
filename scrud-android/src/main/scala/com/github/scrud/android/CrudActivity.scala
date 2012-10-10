package com.github.scrud.android

import action.{OperationResponse, EntityOperation}
import android.os.Bundle
import com.github.triangle.{UpdaterInput, GetterInput, PortableField}
import android.content.Intent
import android.app.Activity
import com.github.scrud.android.view.AndroidConversions._
import android.widget.Toast
import common.UriPath
import validate.ValidationResult

/** A generic Activity for CRUD operations
  * @author Eric Pabst (epabst@gmail.com)
  */
class CrudActivity extends BaseCrudActivity { self =>

  private def populateFromUri(uri: UriPath) {
    future {
      val updaterInput = UpdaterInput(this, contextItems)
      val portableValue = crudApplication.copyFromPersistedEntity(entityType, uri, crudContext).getOrElse(entityType.defaultValue)
      runOnUiThread(this) { portableValue.update(updaterInput) }
    }
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    withExceptionReporting {
      if (savedInstanceState == null) {
        setContentView(crudType.entryLayout)
        val currentPath = currentUriPath
        if (crudApplication.maySpecifyEntityInstance(currentPath, entityType)) {
          populateFromUri(currentPath)
        } else {
          entityType.copy(PortableField.UseDefaults +: contextItems, this)
        }
      }
      bindNormalActionsToViews()
      if (crudApplication.maySpecifyEntityInstance(currentUriPath, entityType)) {
        crudContext.addCachedActivityStateListener(new CachedStateListener {
          def onClearState(stayActive: Boolean) {
            if (stayActive) {
              populateFromUri(currentUriPath)
            }
          }

          def onSaveState(outState: Bundle) {
            entityType.copy(this, outState)
          }

          def onRestoreState(savedInstanceState: Bundle) {
            val portableValue = entityType.copyFrom(savedInstanceState)
            runOnUiThread(self) { portableValue.update(this, contextItems) }
          }
        })
      }
    }
  }

  override def onBackPressed() {
    withExceptionReporting {
      // Save before going back so that the Activity being activated will read the correct data from persistence.
      val writable = crudApplication.newWritable(entityType)
      crudContext.withEntityPersistence(entityType) { persistence =>
        val copyableFields = entityType.copyableTo(writable, contextItemsWithoutUseDefaults)
        val portableValue = copyableFields.copyFrom(this +: contextItemsWithoutUseDefaults)
        if (portableValue.update(ValidationResult.Valid).isValid) {
          val updatedWritable = portableValue.update(writable)
          saveBasedOnUserAction(persistence, updatedWritable)
        } else {
          Toast.makeText(this, res.R.string.data_not_saved_since_invalid_notification, Toast.LENGTH_SHORT).show()
        }
      }
    }
    super.onBackPressed()
  }

  private[scrud] def saveBasedOnUserAction(persistence: CrudPersistence, writable: AnyRef) {
    try {
      val idOpt = entityType.IdField(currentUriPath)
      val newId = persistence.save(idOpt, writable)
      Toast.makeText(this, res.R.string.data_saved_notification, Toast.LENGTH_SHORT).show()
      if (idOpt.isEmpty) setIntent(getIntent.setData(uriWithId(newId)))
    } catch { case e: Throwable => logError("onPause: Unable to store " + writable, e) }
  }

  protected lazy val normalActions = crudApplication.actionsForEntity(entityType).filter {
    case action: EntityOperation => action.entityName != entityType.entityName || action.action != currentAction
    case _ => true
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    withExceptionReporting {
      if (resultCode == Activity.RESULT_OK) {
        //"this" is included in the list so that existing data isn't cleared.
        entityType.copy(GetterInput(OperationResponse(requestCode, data), crudContext, this), this)
      } else {
        debug("onActivityResult received resultCode of " + resultCode + " and data " + data + " for request " + requestCode)
      }
    }
  }
}
