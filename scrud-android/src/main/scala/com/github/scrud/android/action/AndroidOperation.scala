package com.github.scrud.android.action

import _root_.android.app.Activity
import com.github.scrud.platform.PlatformTypes._
import _root_.android.content.{Context, Intent}
import com.github.scrud.android.view.AndroidConversions._
import _root_.android.view.View
import com.github.scrud._
import action._
import android.view.AndroidConversions
import com.github.scrud.android.AndroidCommandContext
import com.github.scrud.EntityName
import com.github.scrud.copy.{SourceType, RepresentationByType}
import com.github.scrud.context.CommandContext

/** Represents an operation that a user can initiate. */
trait AndroidOperation extends Operation {
  /** Runs the operation, given the uri and the current CommandContext. */
  def invoke(uri: UriPath, commandContext: CommandContext) {
    invoke(uri, commandContext.asInstanceOf[AndroidCommandContext].activity)
  }

  /** Runs the operation, given the uri and the current state of the application. */
  def invoke(uri: UriPath, activity: Activity)
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
    val intent = new Intent(action, AndroidConversions.toUri(uriPath, context))
    intent.setClass(context, clazz)
    intent
  }
}

case class RichIntent(intent: Intent) {
  val uriPath: UriPath = intent.getData
}

trait StartActivityOperation extends AndroidOperation {
  def determineIntent(uri: UriPath, activity: Activity): Intent

  def invoke(uri: UriPath, activity: Activity) {
    activity.startActivity(determineIntent(uri, activity))
  }
}

trait BaseStartActivityOperation extends StartActivityOperation {
  def action: String

  def activityClass: Class[_ <: Activity]

  def determineIntent(uri: UriPath, activity: Activity): Intent = AndroidOperation.constructIntent(action, uri, activity, activityClass)
}

/** An Operation that starts an Activity using the provided Intent.
  * @param intent the Intent to use to start the Activity.  It is pass-by-name because the SDK's Intent has a "Stub!" error. */
class StartActivityOperationFromIntent(intent: => Intent) extends StartActivityOperation {
  def determineIntent(uri: UriPath, activity: Activity) = intent
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

  override def determineIntent(uri: UriPath, activity: Activity): Intent =
    super.determineIntent(uri.specify(entityName), activity)
}

//final to guarantee equality is correct
final case class StartEntityIdActivityOperation(entityName: EntityName, action: String, activityClass: Class[_ <: Activity])
  extends BaseStartActivityOperation with EntityOperation {

  override def determineIntent(uri: UriPath, activity: Activity) = super.determineIntent(uri.specifyLastEntityName(entityName), activity)
}

trait StartActivityForResultOperation extends StartActivityOperation {
  def viewIdToRespondTo: ViewKey

  override def invoke(uri: UriPath, activity: Activity) {
    activity.startActivityForResult(determineIntent(uri, activity), viewIdToRespondTo)
  }
}

object StartActivityForResultOperation {
  def apply(view: View, intent: => Intent): StartActivityForResultOperation =
    new StartActivityOperationFromIntent(intent) with StartActivityForResultOperation {
      val viewIdToRespondTo = view.getId
    }
}
