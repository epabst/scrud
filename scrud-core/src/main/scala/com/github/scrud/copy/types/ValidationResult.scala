package com.github.scrud.copy.types

import com.github.scrud.copy.InstantiatingTargetType
import com.github.scrud.context.CommandContext

/**
 * The target of a validation check.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/13/14
 *         Time: 10:54 AM
 */
case class ValidationResult(numInvalid: Int) {
  val isValid: Boolean = numInvalid == 0

  def +(isValid: Boolean): ValidationResult = if (isValid) this else ValidationResult(numInvalid + 1)
}

case object ValidationResult extends InstantiatingTargetType[ValidationResult] {
  /** The result for valid data.  It is capitalized so it can be used in case statements. */
  val Valid: ValidationResult = ValidationResult(0)

  def makeTarget(commandContext: CommandContext) = Valid
}
