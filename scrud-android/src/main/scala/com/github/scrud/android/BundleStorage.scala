package com.github.scrud.android

import com.github.scrud.copy.{RepresentationByType, StorageType}

/**
 * A StorageType for [[android.os.Bundle]].
 * Created by eric on 5/9/14.
 */
object BundleStorage extends StorageType with RepresentationByType[Nothing]
