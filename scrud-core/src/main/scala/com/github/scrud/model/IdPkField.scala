package com.github.scrud.model

import com.github.scrud.copy.{TypedSourceField, TypedTargetField}
import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.context.RequestContext

/**
 * A TargetField and SourceField for an IdPk.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/8/14
 *         Time: 11:27 PM
 */

object IdPkField extends TypedTargetField[IdPk,ID] with TypedSourceField[IdPk,ID] {
  /** Get some value or None from the given source. */
  def findFieldValue(sourceData: IdPk, context: RequestContext) = sourceData.id

  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  def updateFieldValue(target: IdPk, valueOpt: Option[ID], context: RequestContext) =
    target.withId(valueOpt)
}
