package com.github.scrud.android.action

import android.view.Menu
import com.github.scrud.state.StateVar
import com.github.scrud.action.PlatformCommand
import com.github.scrud.android.state.ActivityWithState
import android.content.Context

/** An Activity that has an options menu.
  * This no longer supports Android 2.
  * The options menu in Android 3 can be left visible all the time until invalidated.
  * When the options menu changes, invoke {{{this.optionsMenuCommands = ...}}}
  * @author Eric Pabst (epabst@gmail.com)
  */
trait OptionsMenuActivity extends ActivityWithState with AndroidNotification {
  override def context: Context = this

  /** The Commands to be used if they haven't been set yet. */
  protected def defaultOptionsMenuCommands: List[PlatformCommand]

  // Use a StateVar to make it thread-safe
  private object OptionsMenuCommandsVar extends StateVar[List[PlatformCommand]]

  final def optionsMenuCommands: List[PlatformCommand] = OptionsMenuCommandsVar.get(activityState).getOrElse(defaultOptionsMenuCommands)

  def optionsMenuCommands_=(newValue: List[PlatformCommand]) {
    OptionsMenuCommandsVar.set(activityState, newValue)
    invalidateOptionsMenu()
  }

  private[action] def populateMenu(menu: Menu, commands: List[PlatformCommand]) {
    for ((command, index) <- commands.zip(Stream.from(0))) {
      val menuItem = command.title.fold(menu.add(0, command.commandNumber, index, ""))(menu.add(0, command.commandNumber, index, _))
      command.icon.map(icon => menuItem.setIcon(icon))
    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    withExceptionReporting {
      populateMenu(menu, optionsMenuCommands)
    }
    true
  }
}
