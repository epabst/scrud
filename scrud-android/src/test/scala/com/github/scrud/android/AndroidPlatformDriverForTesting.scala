package com.github.scrud.android

import com.github.scrud.android.testres.R

/**
 * An AndroidPlatformDriver for use when testing.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 6/21/14
 */
object AndroidPlatformDriverForTesting extends AndroidPlatformDriver(classOf[R])
