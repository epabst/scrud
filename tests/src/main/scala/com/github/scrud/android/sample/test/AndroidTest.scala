package com.github.scrud.android.sample.test

import android.test.AndroidTestCase
import junit.framework.Assert._

/**
 * A simple AndroidTestCase.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 9/25/12
 * Time: 5:22 AM
 */
class AndroidTest extends AndroidTestCase {
  def testPackageIsCorrect() {
    assertEquals("com.github.scrud.android.sample", getContext.getPackageName)
  }
}
