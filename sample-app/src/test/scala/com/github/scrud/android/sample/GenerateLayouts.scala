package com.github.scrud.android.sample

import com.github.scrud.android.generate.CrudUIGenerator
import com.github.scrud.android.AndroidPlatformDriver

/** A layout generator for the application.
  * @author Eric Pabst (epabst@gmail.com)
  */

object GenerateLayouts {
  def main(args: Array[String]) {
    CrudUIGenerator.generateLayouts(new SampleApplication(new AndroidPlatformDriver(classOf[R])), classOf[SampleAndroidApplication])
  }
}