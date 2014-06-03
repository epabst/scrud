package com.github.scrud.android.action

import com.github.scrud.context.CommandContextDelegator
import com.github.scrud.android.AndroidCommandContext
import android.content.Context

/**
 * A CommandContextHolder for Android.
 * Created by eric on 5/31/14.
 */
trait AndroidCommandContextDelegator extends CommandContextDelegator with AndroidNotification {
  protected def commandContext: AndroidCommandContext

  override def context: Context = commandContext.context
}
