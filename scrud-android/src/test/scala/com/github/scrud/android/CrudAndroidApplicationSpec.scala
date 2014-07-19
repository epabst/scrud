package com.github.scrud.android

import com.github.scrud.android.generate.CrudUIGeneratorForTesting
import org.robolectric.annotation.Config
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.junit.Test
import android.content.Intent
import com.github.scrud.util.Common
import com.github.scrud.context.SharedContext

/**
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 7/18/14
 */
@RunWith(classOf[CustomRobolectricTestRunner])
@Config(manifest = "target/generated/AndroidManifest.xml")
class CrudAndroidApplicationSpec extends CrudUIGeneratorForTesting with ScrudRobolectricSpecBase {
  @Test
  def waitUntilIdleMustWaitForFutures() {
    val activity = Robolectric.buildActivity(classOf[CrudActivityForRobolectric]).
      withIntent(new Intent(Intent.ACTION_MAIN)).get()
    val commandContext = activity.commandContext
    val sharedContext: SharedContext = activity.androidApplication
    val logging = sharedContext.applicationName
    val map = new scala.collection.concurrent.TrieMap[String,Boolean]
    commandContext.runOnUiThread {
      Thread.sleep(50)
      map.put("Scrud UI", true)
    }
    Robolectric.getUiThreadScheduler.post(Common.toRunnable("uiThreadScheduler task", logging) {
      Thread.sleep(100)
      map.put("Robolectric UI", true)
    })
    Robolectric.getBackgroundScheduler.post(Common.toRunnable("backgroundThreadScheduler task", logging) {
      Thread.sleep(150)
      map.put("Robolectric Background", true)
    })
    sharedContext.future {
      Thread.sleep(300)
      map.put("SharedContext Future", true)
    }
    commandContext.future {
      Thread.sleep(400)
      map.put("CommandContext Future", true)
    }
    commandContext.waitUntilIdle()
    map.keySet.toSeq.sorted must be (Set("Scrud UI", "Robolectric UI", "Robolectric Background",
      "SharedContext Future", "CommandContext Future").toSeq.sorted)
  }
}
