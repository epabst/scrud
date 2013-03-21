package com.github.scrud.android.action

import android.view.Menu
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicBoolean
import com.github.scrud.state.StateVar
import com.github.scrud.action.Command
import com.github.scrud.android.state.ActivityWithState

/** An Activity that has an options menu.
  * This is intended to handle both Android 2 and 3.
  * The options menu in Android 3 can be left visible all the time until invalidated.
  * When the options menu changes, invoke {{{this.optionsMenuCommands = ...}}}
  * @author Eric Pabst (epabst@gmail.com)
  */
trait OptionsMenuActivity extends ActivityWithState with AndroidNotification {
  def context = this

  /** The Commands to be used if they haven't been set yet. */
  protected def defaultOptionsMenuCommands: List[Command]

  // Use a StateVar to make it thread-safe
  private object OptionsMenuCommandsVar extends StateVar[List[Command]]

  final def optionsMenuCommands: List[Command] = OptionsMenuCommandsVar.get(activityState).getOrElse(defaultOptionsMenuCommands)

  def optionsMenuCommands_=(newValue: List[Command]) {
    OptionsMenuCommandsVar.set(activityState, newValue)
    invalidateOptionsMenuMethod.map(_.invoke(this)).getOrElse(recreateInPrepare.set(true))
  }

  private val recreateInPrepare = new AtomicBoolean(false)
  private lazy val invalidateOptionsMenuMethod: Option[Method] =
    try { Option(getClass.getMethod("invalidateOptionsMenu"))}
    catch { case _: Exception => None }

  private[action] def populateMenu(menu: Menu, commands: List[Command]) {
    for ((command, index) <- commands.zip(Stream.from(0))) {
      val menuItem = command.title.map(menu.add(0, command.commandNumber, index, _)).getOrElse(menu.add(0, command.commandNumber, index, ""))
      command.icon.map(icon => menuItem.setIcon(icon))
    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    withExceptionReporting {
      populateMenu(menu, optionsMenuCommands)
    }
    true
  }

  override def onPrepareOptionsMenu(menu: Menu) = {
    if (recreateInPrepare.getAndSet(false)) {
      withExceptionReporting {
        menu.clear()
        populateMenu(menu, optionsMenuCommands)
      }
      true
    } else {
      super.onPrepareOptionsMenu(menu)
    }
  }
}
