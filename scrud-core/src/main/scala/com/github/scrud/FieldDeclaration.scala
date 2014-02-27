package com.github.scrud

import com.github.scrud.types.QualifiedType
import com.github.scrud.copy.{AdaptableFieldConvertible, Representation}
import com.github.scrud.platform.PlatformDriver

/**
 * A field declaration
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/17/14
 *         Time: 11:58 PM
 */
class FieldDeclaration[V](entityName: EntityName, fieldName: String, qualifiedType: QualifiedType[V], val representations: Seq[Representation[V]], platformDriver: PlatformDriver)
  extends EntityField[V](entityName, fieldName, qualifiedType) with BaseFieldDeclaration with AdaptableFieldConvertible[V] {

  /**
   * Converts this [[com.github.scrud.copy.AdaptableField]].
   * @return the field
   */
  val toAdaptableField = platformDriver.field(entityName, fieldName, qualifiedType, representations)
}
