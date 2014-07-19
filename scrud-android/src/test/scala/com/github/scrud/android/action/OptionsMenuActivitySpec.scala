package com.github.scrud.android.action

import org.junit.runner.RunWith
import org.junit.Test
import org.scalatest.matchers.MustMatchers
import android.app.Activity
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => eql, _}
import android.view.{MenuItem, Menu}
import com.github.scrud.action.{ActionKey, PlatformCommand}
import com.github.scrud.android.CustomRobolectricTestRunner
import com.github.scrud.util.ExternalLogging
import com.github.scrud.ApplicationNameForTesting
import org.robolectric.annotation.Config
import org.robolectric.Robolectric
import android.content.Context

/** A behavior specification for [[com.github.scrud.android.action.OptionsMenuActivity]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
@Config(manifest = "target/generated/AndroidManifest.xml")
class OptionsMenuActivitySpec extends MustMatchers with MockitoSugar {
  @Test
  def mustUseLatestOptionsMenuForCreate() {
    val activity = Robolectric.buildActivity(classOf[StubOptionsMenuActivity]).create().get()
    activity.optionsMenuCommands = List(PlatformCommand(ActionKey("command1"), None, Some(10)))

    val menu = mock[Menu]
    val menuItem = mock[MenuItem]
    stub(menu.add(anyInt(), anyInt(), anyInt(), anyInt())).toReturn(menuItem)
    activity.onCreateOptionsMenu(menu)
    verify(menu, times(1)).add(anyInt(), eql(10), anyInt(), anyInt())
  }

  @Test
  def mustCallInvalidateOptionsMenuAndNotRepopulateForAndroid3WhenSet() {
    val activity = Robolectric.buildActivity(classOf[StubOptionsMenuActivity]).create().get()
    activity.optionsMenuCommands = List(mock[PlatformCommand])
    activity.invalidated must be (true)
    activity.populated must be (0)

    val menu = mock[Menu]
    activity.onPrepareOptionsMenu(menu)
    activity.populated must be (0)
    verify(menu, never()).clear()
  }

  @Test
  def mustNotRepopulateInPrepareWhenNotSet_Android3() {
    val activity = Robolectric.buildActivity(classOf[StubOptionsMenuActivity]).create().get()
    val menu = mock[Menu]
    activity.onPrepareOptionsMenu(menu)
    verify(menu, never()).clear()
    activity.populated must be (0)
  }
}

class StubOptionsMenuActivity extends Activity with OptionsMenuActivity { self =>
  override val notification: AndroidNotification =
    new AndroidNotificationForRobolectric {
      override def context: Context = self

      override protected val loggingDelegate: ExternalLogging = ApplicationNameForTesting
    }

  protected val defaultOptionsMenuCommands = Nil
  var invalidated = false
  var populated: Int = 0

  //this will be called using reflection
  override def invalidateOptionsMenu() {
    invalidated = true
  }

  override private[action] def populateMenu(menu: Menu, actions: List[PlatformCommand]) {
    populated +=  1
    super.populateMenu(menu, actions)
  }
}
