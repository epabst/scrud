package com.github.scrud.copy.types

import com.github.scrud.copy._
import scala.collection.parallel.mutable
import com.github.scrud.{FieldDeclaration, BaseFieldDeclaration, EntityName}
import scala.Some
import com.github.scrud.context.RequestContext

/**
 * A target and source for data (especially useful when testing).
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:54 PM
 */
class MapStorage extends AnyRef {
  def this(fieldTuples: (BaseFieldDeclaration,Option[Any])*) {
    this()
    for ((declaration, valueOpt) <- fieldTuples) {
      put(declaration.entityName, declaration.fieldName, valueOpt)
    }
  }
  
  def this(entityName: EntityName, tuples: (String,Option[Any])*) {
    this()
    for ((fieldName, valueOpt) <- tuples) {
      put(entityName, fieldName, valueOpt)
    }
  }

  private val map = new mutable.ParHashMap[String,Any]

  def get(entityName: EntityName, fieldName: String): Option[Any] = map.get(toKey(entityName, fieldName))

  def get[V](fieldDeclaration: FieldDeclaration[V]): Option[V] =
    get(fieldDeclaration.entityName, fieldDeclaration.fieldName).asInstanceOf[Option[V]]

  def put(entityName: EntityName, fieldName: String, valueOpt: Option[_]) = {
    valueOpt match {
      case Some(value) => map.put(toKey(entityName, fieldName), value)
      case None => map.remove(toKey(entityName, fieldName))
    }
  }

  private def toKey(entityName: EntityName, fieldName: String): String = {
    entityName.name + "." + fieldName
  }

  override def equals(other: Any) = other match {
    case otherMapStorage: MapStorage =>
      map.equals(otherMapStorage.map)
    case _ =>
      false
  }

  override def hashCode() = map.hashCode() + 13

  override def toString = map.mkString("MapStorage(", ", ", ")")
}

/** This is a reference to the storage type that the class MapStorage represents. */
case object MapStorage extends StorageType with RepresentationByType[Nothing] with InstantiatingTargetType[MapStorage] {
  override def makeTarget(requestContext: RequestContext) = new MapStorage()
}
