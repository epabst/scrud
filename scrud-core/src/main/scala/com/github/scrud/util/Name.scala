package com.github.scrud.util

/**
 * Something that represents a name of some sort.
 * This provides useful conversions for various formats.
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/27/14
 *         Time: 11:16 PM
 */
trait Name {
  /** A name as a Java identifier such as PoliceOfficer (pascal case) or crescentWrench (camel case). */
  def name: String

  override def equals(p1: scala.Any): Boolean

  override def hashCode(): Int

  override val toString = name

  /** This will need to be overridden in many cases. */
  val toPlural: String = name + "s"

  /** The name but with the first letter lower-case. */
  lazy val toCamelCase: String = name.charAt(0).toLower + name.substring(1)

  lazy val toDisplayableString: String = name.replaceAll("([a-z])([A-Z])", "$1 $2")
}

object Name {
  def apply(name: String): Name = {
    val _name = name
    new Name {
      /** A name as a Java identifier such as PoliceOfficer (pascal case) or crescentWrench (camel case). */
      override def name = _name
    }
  }
}
