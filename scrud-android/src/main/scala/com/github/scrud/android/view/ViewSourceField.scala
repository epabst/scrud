package com.github.scrud.android.view

import com.github.scrud.copy.{NestedSourceField, SourceField}

/**
 * A SourceField for an Android View.
 * @author Eric Pabst (epabst@gmail.com)
 */
trait ViewSourceField[V] extends SourceField[V] {
  def forSourceView(viewSpecifier: ViewSpecifier): NestedSourceField[V] =
    new NestedSourceField[V](viewSpecifier, this)
}
