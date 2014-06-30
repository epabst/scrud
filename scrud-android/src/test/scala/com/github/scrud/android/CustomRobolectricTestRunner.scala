package com.github.scrud.android

import com.github.scrud.android.generate.CrudUIGeneratorForTesting
import org.robolectric.RobolectricTestRunner

/**
 * Custom Robolectric test runner that refers to the directory with AndroidManifest.xml and the res directory.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/28/12
 * Time: 6:22 PM
 */
class CustomRobolectricTestRunner(testClass: Class[_]) extends RobolectricTestRunner({
  CrudUIGeneratorForTesting.generateLayoutsIfMissing()
  testClass
})
