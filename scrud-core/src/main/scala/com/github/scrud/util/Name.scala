package com.github.scrud.util

import scala.util.matching.Regex

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

  /** The name without spaces and with the first letter as lower-case. */
  lazy val toCamelCase: String = toTitleCase.charAt(0).toLower + toTitleCase.substring(1)

  /** The name without spaces and with the first letter as upper-case. */
  lazy val toTitleCase: String = {
    Name.wordRegex.findAllMatchIn(name).map { aMatch =>
      val matched = aMatch.matched
      matched.charAt(0).toUpper + matched.substring(1)
    }.mkString.filter(_.isUnicodeIdentifierPart)
  }

  /** The name lowercase with underscores between words. */
  lazy val toSnakeCase: String = Name.titleCaseWordRegex.replaceAllIn(toTitleCase, "_" + _.matched.toLowerCase).stripPrefix("_")

  lazy val toDisplayableString: String = {
    var makeUpperCase = true
    val displayName = name.collect {
      case c if Character.isUpperCase(c) =>
        makeUpperCase = false
        " " + c
      case '_' =>
        makeUpperCase = true
        " "
      case c if makeUpperCase =>
        makeUpperCase = false
        Character.toUpperCase(c)
      case c => c.toString
    }.mkString
    displayName.stripPrefix(" ")
  }
}

object Name {
  private[Name] val wordRegex: Regex = "[\\w']+".r
  private[Name] val titleCaseWordRegex: Regex = "\\p{javaUpperCase}+(?!\\p{javaLowerCase})|\\p{javaUpperCase}\\p{javaLowerCase}+".r

  def apply(name: String): Name = {
    val _name = name
    new Name {
      /** A name as a Java identifier such as PoliceOfficer (pascal case) or crescentWrench (camel case). */
      override def name = _name
    }
  }
}
