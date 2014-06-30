package com.github.scrud.android

import org.scalatest._
import com.github.scrud.util.{Logging, CrudMockitoSugar}

/**
 * A mix-in for all scrud-android specs that use Robolectric (with JUnit).
 * <p>
 *   If using Robolectric, the class should be annotated with the following (unfortunately not inheritable):
 *   @RunWith(classOf[CustomRobolectricTestRunner])
 *   @Config(manifest = "target/generated/AndroidManifest.xml")
 * </p>
 * <p>
 *   If using ScalaTest without Robolectric, extend FunSpec.
 * </p>
 *
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 6/30/14
 */
trait ScrudRobolectricSpec extends MustMatchers with CrudMockitoSugar with Logging {
  protected override val logTag = getClass.getSimpleName
}
