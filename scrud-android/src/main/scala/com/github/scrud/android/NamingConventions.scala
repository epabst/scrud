package com.github.scrud.android

import com.github.scrud.EntityName

/** A utility that defines the naming conventions for Crud applications.
  * @author Eric Pabst (epabst@gmail.com)
  */

object NamingConventions {
  def toLayoutPrefix(entityName: EntityName): String = entityName.name.collect {
    case c if (c.isUpper) => "_" + c.toLower
    case c if (Character.isJavaIdentifierPart(c)) => c.toString
  }.mkString.stripPrefix("_")
}