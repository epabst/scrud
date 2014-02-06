package com.github.scrud.sample

import com.github.scrud.platform.PlatformDriver
import com.github.scrud.EntityNavigation

/**
 * The EntityNavigation.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/6/14
 *         Time: 12:51 AM
 */
class SampleEntityNavigation(platformDriver: PlatformDriver)
    extends EntityNavigation(SampleApplication, new SampleEntityTypeMap(platformDriver), platformDriver)
