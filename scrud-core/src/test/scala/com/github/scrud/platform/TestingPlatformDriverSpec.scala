package com.github.scrud.platform

//import org.junit.runner.RunWith

/**
 * The behavior specification for [[com.github.scrud.platform.TestingPlatformDriver]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/12/13
 *         Time: 11:16 AM
 */
//@RunWith(classOf[JUnitRunner])
class TestingPlatformDriverSpec extends PlatformDriverContractSpec {
  protected def makePlatformDriver() = TestingPlatformDriver
}
