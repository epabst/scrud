package com.github.scrud

import com.github.scrud.platform.PlatformTypes._
import com.github.scrud.platform.IdFormat
import java.net.URI

/** A convenience wrapper for UriPath.
  * It helps in that UriPath.EMPTY is null when running unit tests, and helps prepare for multi-platform support.
  * @author Eric Pabst (epabst@gmail.com)
  */
case class UriPath(segments: String*) {
  def /(segment: String): UriPath = UriPath(segments :+ segment:_*)

  def /(entityName: EntityName): UriPath = this / entityName.name

  def /(id: ID): UriPath = this / id.toString

  @deprecated("use UriPath.specify(this, finalSegments: _*)", since = "03/06/2014")
  def specify(finalSegments: String*): UriPath = UriPath.specify(this, finalSegments: _*)

  @deprecated("use UriPath.specify(this, entityName)", since = "03/06/2014")
  def specify(entityName: EntityName): UriPath = UriPath.specify(this, entityName)

  @deprecated("use UriPath.specify(this, entityName, id)", since = "03/06/2014")
  def specify(entityName: EntityName, id: ID): UriPath = UriPath.specify(this, entityName, id)

  @deprecated("use UriPath.specifyLastEntityName(this, entityName)", since = "03/06/2014")
  def specifyLastEntityName(entityName: EntityName): UriPath = UriPath.specifyLastEntityName(this, entityName)

  @deprecated("use UriPath.lastEntityNameOption(this)", since = "03/06/2014")
  lazy val lastEntityNameOption: Option[EntityName] = UriPath.lastEntityNameOption(this)

  @deprecated("use UriPath.lastEntityNameOrFail(this)", since = "03/06/2014")
  def lastEntityNameOrFail: EntityName = UriPath.lastEntityNameOrFail(this)

  @deprecated("use UriPath.findId(this, entityName)", since = "03/06/2014")
  def findId(entityName: EntityName): Option[ID] = UriPath.findId(this, entityName)

  override lazy val toString = segments.mkString("/", "/", "")
}

object UriPath {
  private lazy val idFormat = IdFormat

  val EMPTY: UriPath = UriPath()

  private def toOption(string: String): Option[String] = if (string == "") None else Some(string)

  def apply(string: String): UriPath = UriPath(toOption(string.stripPrefix("/")).map(_.split("/").toSeq).getOrElse(Nil):_*)

  def apply(entityName: EntityName): UriPath = UriPath(entityName.name)

  def apply(entityName: EntityName, id: ID): UriPath = UriPath(entityName.name, id.toString)

  private[UriPath] def replacePathSegments(uri: UriPath, f: Seq[String] => Seq[String]): UriPath = {
    val path = f(uri.segments)
    UriPath(path: _*)
  }

  def specify(uri: UriPath, finalSegments: String*): UriPath =
    replacePathSegments(uri, _.takeWhile(_ != finalSegments.head) ++ finalSegments.toList)

  def specify(uri: UriPath, entityName: EntityName): UriPath = UriPath.specify(uri, entityName.name)

  def specify(uri: UriPath, entityName: EntityName, id: ID): UriPath =
    specify(uri, entityName.name, id.toString)

  def specify(uri: UriPath, entityName: EntityName, idOpt: Option[ID]): UriPath =
    idOpt.fold(specify(uri, entityName))(specify(uri, entityName, _))

  def specify(uri: UriPath, idOpt: Option[ID]): UriPath = idOpt.fold(uri)(id => specify(uri, id.toString))

  def specifyLastEntityName(uri: UriPath, entityName: EntityName): UriPath =
    UriPath.specify(uri, entityName.name +: findId(uri, entityName).map(_.toString).toList:_*)

  def findId(uri: UriPath, entityName: EntityName): Option[ID] =
    uri.segments.dropWhile(_ != entityName.name).toList match {
      case _ :: idString :: tail => idFormat.toValue(idString).toOption
      case _ => None
    }

  def lastEntityNameOption(uri: UriPath): Option[EntityName] =
    uri.segments.reverse.find(idFormat.toValue(_).isFailure).map(EntityName(_))

  def lastEntityNameOrFail(uri: UriPath): EntityName = lastEntityNameOption(uri).getOrElse {
    throw new IllegalArgumentException("an EntityName must be specified in the URI but uri=" + uri)
  }

  //todo delete once UriPath is replaced by URI
  implicit def uriPathToUri(uriPath: UriPath): URI = throw new UnsupportedOperationException("todo implement")
  //todo delete once UriPath is replaced by URI
  implicit def uriToUriPath(uriPath: URI): UriPath = throw new UnsupportedOperationException("todo implement")
}
