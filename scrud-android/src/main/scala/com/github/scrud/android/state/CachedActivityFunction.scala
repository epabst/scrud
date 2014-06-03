package com.github.scrud.android.state

import android.os.Bundle
import com.github.scrud.util.CachedFunction
import com.github.scrud.CommandContext
import com.github.scrud.android.AndroidCommandContext

/** A Function whose results are cached in each Activity. */
trait CachedActivityFunction[A, B] {
  private val cachedFunctionVar = new ActivityVar[CachedFunction[A, B]]

  /** Specify the actual function to use when the result has not been cached for a given Activity. */
  protected def evaluate(input: A): B

  private def cachedFunction(commandContext: AndroidCommandContext) = cachedFunctionVar.getOrSet(commandContext.stateHolder, {
    val cachedFunction = CachedFunction[A, B](evaluate)
    commandContext.addCachedActivityStateListener(new CachedStateListener {
      /** Save any cached state into the given bundle before switching context. */
      def onSaveState(outState: Bundle) {}

      /** Restore cached state from the given bundle before switching back context. */
      def onRestoreState(savedInstanceState: Bundle) {}

      /** Drop cached state.  If stayActive is true, then the state needs to be functional. */
      def onClearState(stayActive: Boolean) {
        cachedFunction.clear()
      }
    })
    cachedFunction
  })

  def apply(commandContext: AndroidCommandContext, input: A): B = {
    val function = cachedFunction(commandContext)
    function.apply(input)
  }

  def setResult(commandContext: AndroidCommandContext, input: A, result: B) {
    cachedFunction(commandContext).setResult(input, result)
  }

  def clear(commandContext: CommandContext) {
    cachedFunctionVar.clear(commandContext.stateHolder)
  }
}
