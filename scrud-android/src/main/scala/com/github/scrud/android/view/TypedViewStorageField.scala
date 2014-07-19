package com.github.scrud.android.view

import scala.xml.NodeSeq
import android.view.View

/**
 * A Field for an Android View that supports both a SourceType and TargetType.
 * @param defaultLayout the default layout used as an example and by [[com.github.scrud.android.generate.CrudUIGenerator]].
 * @author Eric Pabst (epabst@gmail.com)
 * @tparam S storage class or trait
 * @tparam V value class or trait
 */
abstract class TypedViewStorageField[S <: View,V](defaultLayout: NodeSeq)
  extends TypedViewTargetField[S,V](defaultLayout) with TypedViewSourceField[S,V] with ViewStorageField[V]
