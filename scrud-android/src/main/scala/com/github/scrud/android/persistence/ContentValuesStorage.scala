package com.github.scrud.android.persistence

import com.github.scrud.copy.{RepresentationByType, StorageType}

/**
 * A StorageType for [[android.content.ContentValues]].
 * @see [[com.github.scrud.platform.representation.Persistence]] where a ContentValues is a TargetType for android fields with persistence.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 6/23/14
 */
object ContentValuesStorage extends StorageType with RepresentationByType[Nothing]
