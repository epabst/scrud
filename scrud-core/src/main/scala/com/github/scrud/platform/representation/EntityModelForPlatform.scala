package com.github.scrud.platform.representation

import com.github.scrud.copy.{RepresentationByType, StorageType}

/**
  * A StorageType for copying to and from a model entity that is platform-dependent.
  * @author Eric Pabst (epabst@gmail.com)
  *         Date: 12/11/13
  *         Time: 9:16 AM
  */
object EntityModelForPlatform extends StorageType with RepresentationByType[Nothing]
