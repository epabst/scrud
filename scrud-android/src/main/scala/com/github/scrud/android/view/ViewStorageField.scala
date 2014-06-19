package com.github.scrud.android.view

/**
 * A Field for an Android View that supports both a SourceType and TargetType.
 * @author Eric Pabst (epabst@gmail.com)
 * @tparam V value class or trait
 */
trait ViewStorageField[V] extends ViewTargetField[V] with ViewSourceField[V]
