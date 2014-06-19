package com.github.scrud.android.action

import com.github.scrud.context.CommandContextDelegator
import com.github.scrud.android.{CrudAndroidApplication, AndroidPlatformDriver, AndroidCommandContext}

/**
 * A CommandContextHolder for Android.
 * Created by eric on 5/31/14.
 */
trait AndroidCommandContextDelegator extends CommandContextDelegator {
  protected def commandContext: AndroidCommandContext

  def androidApplication: CrudAndroidApplication = commandContext.androidApplication

  override lazy val platformDriver: AndroidPlatformDriver = super.platformDriver.asInstanceOf[AndroidPlatformDriver]
}
