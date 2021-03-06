package com.github.scrud.android.generate

import com.github.scrud.EntityType
import com.github.scrud.android.view.AndroidResourceAnalyzer._
import com.github.scrud.persistence.EntityTypeMap

/**
 * View information about an [[com.github.scrud.EntityType]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 4/21/14
 *         Time: 7:16 PM
 */

case class EntityTypeViewInfo(entityType: EntityType, entityTypeMap: EntityTypeMap) {
  val entityName = entityType.entityName
  lazy val layoutPrefix = entityType.entityName.toSnakeCase
  lazy val rIdClasses: Seq[Class[_]] = detectRIdClasses(entityType.getClass)
  lazy val entityFieldInfos: List[EntityFieldInfo] = entityType.fieldDeclarations.map(EntityFieldInfo(_, rIdClasses, entityTypeMap)).toList
}
