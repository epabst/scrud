package com.github.scrud.android.action

import _root_.android.app.Activity
import com.github.scrud.platform.PlatformTypes._
import _root_.android.content.{Context, Intent}
import com.github.scrud.android.view.AndroidConversions._
import _root_.android.view.View
import com.github.triangle.Field
import com.github.triangle.PortableField._
import com.github.scrud._
import action._
import com.github.scrud.android.AndroidCrudContext
import com.github.scrud.EntityName

/** Represents an operation that a user can initiate. */
trait AndroidOperation extends Operation {
  /** Runs the operation, given the uri and the current CrudContext. */
  def invoke(uri: UriPath, crudContext: CrudContext) {
    invoke(uri, crudContext.asInstanceOf[AndroidCrudContext].activityContext.asInstanceOf[ActivityWithState])
  }

  /** Runs the operation, given the uri and the current state of the application. */
  def invoke(uri: UriPath, activity: ActivityWithState)
}

object AndroidOperation {
  val CreateActionName: String = Intent.ACTION_INSERT
  val ListActionName: String = Intent.ACTION_PICK
  val DisplayActionName: String = Intent.ACTION_VIEW
  val UpdateActionName: String = Intent.ACTION_EDIT
  val DeleteActionName: String = Intent.ACTION_DELETE

  implicit def toRichItent(intent: Intent) = new RichIntent(intent)

  //this is a workaround because Robolectric doesn't handle the full constructor
  def constructIntent(action: String, uriPath: UriPath, context: Context, clazz: Class[_]): Intent = {
    val intent = new Intent(action, uriPath)
    intent.setClass(context, clazz)
    intent
  }
}

case class RichIntent(intent: Intent) {
  val uriPath: UriPath = intent.getData
}

trait StartActivityOperation extends AndroidOperation {
  def determineIntent(uri: UriPath, activity: ActivityWithState): Intent

  def invoke(uri: UriPath, activity: ActivityWithState) {
    activity.startActivity(determineIntent(uri, activity))
  }
}

trait BaseStartActivityOperation extends StartActivityOperation {
  def action: String

  def activityClass: Class[_ <: Activity]

  def determineIntent(uri: UriPath, activity: ActivityWithState): Intent = AndroidOperation.constructIntent(action, uri, activity, activityClass)
}

/** An Operation that starts an Activity using the provided Intent.
  * @param intent the Intent to use to start the Activity.  It is pass-by-name because the SDK's Intent has a "Stub!" error. */
class StartActivityOperationFromIntent(intent: => Intent) extends StartActivityOperation {
  def determineIntent(uri: UriPath, activity: ActivityWithState) = intent
}

//final to guarantee equality is correct
final case class StartNamedActivityOperation(action: String, activityClass: Class[_ <: Activity]) extends BaseStartActivityOperation

trait EntityOperation extends AndroidOperation {
  def entityName: EntityName
  def action: String
}

//final to guarantee equality is correct
final case class StartEntityActivityOperation(entityName: EntityName, action: String, activityClass: Class[_ <: Activity])
  extends BaseStartActivityOperation with EntityOperation {

  override def determineIntent(uri: UriPath, activity: ActivityWithState): Intent =
    super.determineIntent(uri.specify(entityName), activity)
}

//final to guarantee equality is correct
final case class StartEntityIdActivityOperation(entityName: EntityName, action: String, activityClass: Class[_ <: Activity])
  extends BaseStartActivityOperation with EntityOperation {

  override def determineIntent(uri: UriPath, activity: ActivityWithState) = super.determineIntent(uri.upToOptionalIdOf(entityName), activity)
}

trait StartActivityForResultOperation extends StartActivityOperation {
  def viewIdToRespondTo: ViewKey

  override def invoke(uri: UriPath, activity: ActivityWithState) {
    activity.startActivityForResult(determineIntent(uri, activity), viewIdToRespondTo)
  }
}

object StartActivityForResultOperation {
  def apply(view: View, intent: => Intent): StartActivityForResultOperation =
    new StartActivityOperationFromIntent(intent) with StartActivityForResultOperation {
      val viewIdToRespondTo = view.getId
    }
}

/** The response to a [[com.github.scrud.android.action.StartActivityForResultOperation]].
  * This is used by [[com.github.scrud.android.CrudActivity]]'s startActivityForResult.
  */
case class OperationResponse(viewIdRespondingTo: ViewKey, intent: Intent)

/** An extractor to get the OperationResponse from the items being copied from. */
object OperationResponseExtractor extends Field(identityField[OperationResponse])
