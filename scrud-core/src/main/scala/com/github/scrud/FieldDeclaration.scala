package com.github.scrud

import com.github.scrud.types.QualifiedType
import com.github.scrud.copy._
import com.github.scrud.platform.PlatformDriver
import com.github.scrud.context.CommandContext

/**
 * A field declaration
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/17/14
 *         Time: 11:58 PM
 */
case class FieldDeclaration[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V], representations: Seq[Representation[V]], platformDriver: PlatformDriver)
  extends BaseFieldDeclaration with AdaptableFieldConvertible[V] {

  /**
   * Converts this [[com.github.scrud.copy.AdaptableField]].
   * @return the field
   */
  val toAdaptableField = platformDriver.field(entityName, fieldName, qualifiedType, representations)

  def ->(valueOpt: Option[V]): (this.type, Option[V]) = (this, valueOpt)

  /** Convenience method for finding a field value from a given, applicable source. */
  def findApplicable(sourceType: SourceType, source: AnyRef, sourceUri: UriPath, commandContext: CommandContext): Option[V] = {
    val sourceField = toAdaptableField.sourceFieldOrFail(sourceType)
    sourceField.findValue(source, new CopyContext(sourceUri, commandContext))
  }

  /** Convenience method for getting a non-option field value from a given source. */
  def getRequired(sourceType: SourceType, source: AnyRef, sourceUri: UriPath, commandContext: CommandContext): V = {
    findApplicable(sourceType, source, sourceUri, commandContext).getOrElse(sys.error("no value found"))
  }

  /** Convenience method for updating a field with a given value for a given, applicable target. */
  def updateWithValue[T <: AnyRef](targetType: TargetType, target: T, valueOpt: Option[V], sourceUri: UriPath, commandContext: CommandContext): T = {
    toAdaptableField.targetFieldOrFail(targetType).updateValue(target, None, new CopyContext(sourceUri, commandContext))
  }
}
