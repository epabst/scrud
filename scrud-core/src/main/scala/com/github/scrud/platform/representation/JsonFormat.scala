package com.github.scrud.platform.representation

import com.github.scrud.copy.{RepresentationByType, StorageType}

/**
  * A [[com.github.scrud.platform.representation.SerializedFormat]] for JSON.
  * @author Eric Pabst (epabst@gmail.com)
  *         Date: 12/11/13
  *         Time: 9:16 AM
  */
object JsonFormat extends StorageType with SerializedFormat with RepresentationByType
