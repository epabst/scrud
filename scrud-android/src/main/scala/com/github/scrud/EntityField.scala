package com.github.scrud

object EntityField {
  def fieldName(entityName: EntityName): String = entityName.name.toLowerCase + "_id"
}
