package com.github.scrud.sample

import com.github.scrud.platform.PlatformDriver
import com.github.scrud.EntityNavigation

/**
 * The EntityNavigation.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/6/14
 *         Time: 12:51 AM
 */
class SampleEntityNavigation(override val entityTypeMap: SampleEntityTypeMap)
    extends EntityNavigation(entityTypeMap) {

  def this(platformDriver: PlatformDriver) {
    this(new SampleEntityTypeMap(platformDriver))
  }
}
