package com.github.scrud.copy.types

import com.github.scrud.copy._
import com.github.scrud.context.RequestContext

/**
 * A field for validating data.  It updates a ValidationResult by checking a value.
 * @author Eric Pabst (epabst@gmail.com)
 */
class Validation[V](f: Option[V] => Boolean) extends TypedTargetField[ValidationResult,V] with AdaptableFieldConvertible[V] with Representation[V] {
  /** Updates the {{{target}}} subject using the {{{valueOpt}}} for this field and some context. */
  def updateFieldValue(target: ValidationResult, valueOpt: Option[V], context: RequestContext) = {
    target + isValid(valueOpt)
  }

  def isValid(value: Option[V]): Boolean = f.apply(value)

  /**
   * Converts this [[com.github.scrud.copy.AdaptableField]].
   * @return the field
   */
  val toAdaptableField: ExtensibleAdaptableField[V] = AdaptableField[V](Map.empty, Map(ValidationResult -> this))
}

object Validation {
  def apply[V](isValid: Option[V] => Boolean): Validation[V] = new Validation[V](isValid)

  /** A Validation that requires that the value be defined.
    * It does allow the value to be an empty string, empty list, etc.
    * Example: <pre>field... + required</pre>
    */
  def required[V]: Validation[V] = Validation(_.isDefined)

  /** A Validation that requires that the value be defined and meet criteria.
    * It does allow the value to be an empty string, empty list, etc.
    * Example: <pre>field... + requiredAnd(_ != "")</pre>
    */
  def requiredAnd[V](isValid: V => Boolean): Validation[V] = Validation(_.exists(isValid(_)))

  /** A Validation that requires that the value be defined and not one of the given values.
    * Example: <pre>field... + requiredAndNot("")</pre>
    */
  def requiredAndNot[V](invalidValues: V*): Validation[V] = requiredAnd(!invalidValues.contains(_))

  /** A Validation that requires that the value not be empty (after trimming). */
  lazy val requiredString: Validation[String] = requiredAnd(_.trim != "")
}
