package com.github.scrud.android.state

import android.os.Bundle

/**
 * A listener for cached state events.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 10/20/12
 *         Time: 5:17 PM
 */
trait CachedStateListener {
  /** Save any cached state into the given bundle before switching context. */
  def onSaveState(outState: Bundle)

  /** Restore cached state from the given bundle before switching back context. */
  def onRestoreState(savedInstanceState: Bundle)

  /** Drop cached state.  If stayActive is true, then the state needs to be functional. */
  def onClearState(stayActive: Boolean)
}
