package com.github.scrud.android.generate

import com.github.scrud.android.AndroidPlatformDriver
import com.github.scrud.platform.representation.{SelectUI, EditUI}
import com.github.scrud.copy.{TargetField, TargetType}

case class TargetedFieldInfo[V](targetField: TargetField[V], entityFieldInfo: EntityFieldInfo, targetType: TargetType, fieldPrefix: String = "") {
  import entityFieldInfo._
  private val platformDriver = entityTypeMap.platformDriver.asInstanceOf[AndroidPlatformDriver]

  lazy val viewIdFieldInfo: ViewIdFieldInfo = {
    val viewRef = platformDriver.toViewRef(fieldPrefix, field.fieldName)
    val idString = viewRef.fieldName(rIdClasses)
    val displayName = field.fieldName.toDisplayableString
    ViewIdFieldInfo(idString, displayName, field, targetField, entityTypeMap)
  }

  lazy val viewIdFieldInfos: Seq[ViewIdFieldInfo] =
    if (targetType == EditUI) {
      Seq(viewIdFieldInfo)
    } else {
      nestedEntityTypeViewInfoOpt.
        map(TargetedEntityTypeViewInfo(_, SelectUI, fieldPrefix + field.fieldName.toSnakeCase + "_")).
        fold(Seq(viewIdFieldInfo))(_.viewIdFieldInfos)
    }
}
