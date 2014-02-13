package com.github.scrud.copy.types

/** A PortableField for validating data.  It updates a ValidationResult using a value.
  * @author Eric Pabst (epabst@gmail.com)
  */
class Validation[T](f: Option[T] => Boolean) {
  def isValid(value: Option[T]): Boolean = f.apply(value)
}

object Validation {
  def apply[T](isValid: Option[T] => Boolean): Validation[T] = new Validation[T](isValid)

  /** A Validation that requires that the value be defined.
    * It does allow the value to be an empty string, empty list, etc.
    * Example: <pre>field... + required</pre>
    */
  def required[T]: Validation[T] = Validation(_.isDefined)

  /** A Validation that requires that the value be defined and meet criteria.
    * It does allow the value to be an empty string, empty list, etc.
    * Example: <pre>field... + requiredAnd(_ != "")</pre>
    */
  def requiredAnd[T](isValid: T => Boolean): Validation[T] = Validation(_.exists(isValid(_)))

  /** A Validation that requires that the value be defined and not one of the given values.
    * Example: <pre>field... + requiredAndNot("")</pre>
    */
  def requiredAndNot[T](invalidValues: T*): Validation[T] = requiredAnd(!invalidValues.contains(_))

  /** A Validation that requires that the value not be empty (after trimming). */
  lazy val requiredString: Validation[String] = requiredAnd(_.trim != "")
}
