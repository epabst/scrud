package com.github.scrud.android.generate

import com.github.scrud.copy.TargetType

/**
 * View information about an [[com.github.scrud.EntityType]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 4/21/14
 *         Time: 7:16 PM
 */

case class TargetedEntityTypeViewInfo(entityTypeViewInfo: EntityTypeViewInfo, targetType: TargetType, fieldPrefix: String = "") {
  import entityTypeViewInfo._

  lazy val targetedFieldInfos: List[TargetedFieldInfo[Nothing]] = {
    for {
      entityFieldInfo <- entityFieldInfos
      targetedFieldInfo <- entityFieldInfo.findTargetedFieldInfo(targetType, fieldPrefix)
    } yield targetedFieldInfo
  }

  def isEmpty: Boolean = targetedFieldInfos.isEmpty

  lazy val viewIdFieldInfos: List[ViewIdFieldInfo] = targetedFieldInfos.flatMap(_.viewIdFieldInfos)
}
