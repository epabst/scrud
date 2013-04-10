package com.github.scrud.android

import com.github.scrud.util.ReadyFuture
import com.github.scrud.CrudApplication
import android.content.Context
import state.ActivityStateHolder

/**
 * An [[com.github.scrud.android.AndroidCrudContext]] for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/9/13
 * Time: 10:34 PM
 */
class AndroidCrudContextForTesting(application: CrudApplication,
                                   activity: Context with ActivityStateHolder = new ActivityStateHolderForTesting)
    extends AndroidCrudContext(activity, application) {
  override def future[T](body: => T) = new ReadyFuture[T](body)

  override def reportError(throwable: Throwable) {
    throw throwable
  }
}
