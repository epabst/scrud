package com.github.scrud.platform.representation

import com.github.scrud.copy._

/**
  * A StorageType for copying to and from a model entity.
  * @author Eric Pabst (epabst@gmail.com)
  *         Date: 12/11/13
  *         Time: 9:16 AM
  */
case class ModelGetter[M <: AnyRef,V](getter: M => Option[V])(implicit manifest: Manifest[M])
    extends AdaptableFieldByType[V](Map(EntityModel[M] -> new ModelGetterField[M,V](getter)), Map.empty) with RepresentationByType[V] {
  /** Gets the FieldApplicability that is intrinsic to this Representation.  The PlatformDriver may replace this as needed. */
  override val toPlatformIndependentFieldApplicability = FieldApplicability(Set(EntityModel[M]), Set.empty)
}

class ModelGetterField[M <: AnyRef,V](getter: M => Option[V]) extends TypedSourceField[M,V] {
  /** Get some value or None from the given source. */
  def findFieldValue(sourceData: M, context: CopyContext) = {
    getter(sourceData)
  }
}
