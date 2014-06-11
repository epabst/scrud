package com.github.scrud.android.action

import com.github.scrud.context.CommandContextDelegator
import com.github.scrud.android.{CrudAndroidApplication, AndroidPlatformDriver, AndroidCommandContext}
import android.content.Context

/**
 * A CommandContextHolder for Android.
 * Created by eric on 5/31/14.
 */
trait AndroidCommandContextDelegator extends CommandContextDelegator with AndroidNotification {
  protected def commandContext: AndroidCommandContext

  def androidApplication: CrudAndroidApplication = commandContext.androidApplication

  override lazy val platformDriver: AndroidPlatformDriver = super.platformDriver.asInstanceOf[AndroidPlatformDriver]

  override def context: Context = commandContext.context
}
