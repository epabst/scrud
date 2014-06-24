package com.github.scrud.android

import com.xtremelabs.robolectric.RobolectricTestRunner
import java.io.File

/**
 * Custom Robolectric test runner that refers to the directory with AndroidManifest.xml and the res directory.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/28/12
 * Time: 6:22 PM
 */
class CustomRobolectricTestRunner(testClass: Class[_])
    extends RobolectricTestRunner(testClass, new File("src/test"))
