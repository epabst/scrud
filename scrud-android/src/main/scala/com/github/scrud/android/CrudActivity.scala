package com.github.scrud.android

import action.OperationResponse
import android.os.Bundle
import com.github.triangle.{GetterInput, PortableField}
import android.content.Intent
import android.app.Activity
import android.widget.Toast
import com.github.scrud.ValidationResult
import state.CachedStateListener

/** A generic Activity for CRUD operations
  * @author Eric Pabst (epabst@gmail.com)
  */
class CrudActivity extends BaseCrudActivity { self =>

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    crudContext.withExceptionReporting {
      if (savedInstanceState == null) {
        setContentView(entryLayout)
        val currentPath = currentUriPath
        if (crudApplication.maySpecifyEntityInstance(currentPath, entityType)) {
          populateFromUri(entityType, currentPath)
        } else {
          entityType.copy(PortableField.UseDefaults +: contextItems, this)
        }
      }
      bindNormalActionsToViews()
      if (crudApplication.maySpecifyEntityInstance(currentUriPath, entityType)) {
        crudContext.addCachedActivityStateListener(new CachedStateListener {
          def onClearState(stayActive: Boolean) {
            if (stayActive) {
              populateFromUri(entityType, currentUriPath)
            }
          }

          def onSaveState(outState: Bundle) {
            entityType.copy(this, outState)
          }

          def onRestoreState(savedInstanceState: Bundle) {
            val portableValue = entityType.copyFrom(savedInstanceState)
            crudContext.runOnUiThread { portableValue.update(this, contextItems) }
          }
        })
      }
    }
  }

  override def onBackPressed() {
    crudContext.withExceptionReporting {
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

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    crudContext.withExceptionReporting {
      if (resultCode == Activity.RESULT_OK) {
        //"this" is included in the list so that existing data isn't cleared.
        entityType.copy(GetterInput(OperationResponse(requestCode, data), crudContext, this), this)
      } else {
        debug("onActivityResult received resultCode of " + resultCode + " and data " + data + " for request " + requestCode)
      }
    }
  }
}
