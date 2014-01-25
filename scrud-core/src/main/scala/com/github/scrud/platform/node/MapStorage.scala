package com.github.scrud.platform.node

import com.github.scrud.copy._
import scala.collection.parallel.mutable
import com.github.scrud.EntityName

/**
 * A target and source for data (especially useful when testing).
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/11/13
 *         Time: 9:54 PM
 */
class MapStorage {
  private val map = new mutable.ParHashMap[String,Any]

  def get(entityName: EntityName, fieldName: String) = map.get(toKey(entityName, fieldName))

  def put(entityName: EntityName, fieldName: String, valueOpt: Option[_]) = {
    valueOpt match {
      case Some(value) => map.put(toKey(entityName, fieldName), value)
      case None => map.remove(toKey(entityName, fieldName))
    }
  }

  private def toKey(entityName: EntityName, fieldName: String): String = {
    entityName.name + "." + fieldName
  }
}

object MapStorage extends StorageType