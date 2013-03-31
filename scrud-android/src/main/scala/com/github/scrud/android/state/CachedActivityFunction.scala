package com.github.scrud.android.state

import android.os.Bundle
import com.github.scrud.util.CachedFunction
import com.github.scrud.CrudContext
import com.github.scrud.android.AndroidCrudContext

/** A Function whose results are cached in each Activity. */
trait CachedActivityFunction[A, B] {
  private val cachedFunctionVar = new ActivityVar[CachedFunction[A, B]]

  /** Specify the actual function to use when the result has not been cached for a given Activity. */
  protected def evaluate(input: A): B

  private def cachedFunction(crudContext: AndroidCrudContext) = cachedFunctionVar.getOrSet(crudContext.stateHolder, {
    val cachedFunction = CachedFunction[A, B](evaluate)
    crudContext.addCachedActivityStateListener(new CachedStateListener {
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

  def apply(crudContext: AndroidCrudContext, input: A): B = {
    val function = cachedFunction(crudContext)
    function.apply(input)
  }

  def setResult(crudContext: AndroidCrudContext, input: A, result: B) {
    cachedFunction(crudContext).setResult(input, result)
  }

  def clear(crudContext: CrudContext) {
    cachedFunctionVar.clear(crudContext.stateHolder)
  }
}
