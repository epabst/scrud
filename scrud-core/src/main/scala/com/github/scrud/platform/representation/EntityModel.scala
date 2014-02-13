package com.github.scrud.platform.representation

import com.github.scrud.copy.{RepresentationByType, StorageType}

/**
  * A StorageType for copying to and from a model entity.
  * @author Eric Pabst (epabst@gmail.com)
  *         Date: 12/11/13
  *         Time: 9:16 AM
  */
object EntityModel extends StorageType with RepresentationByType
