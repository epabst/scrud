package com.github.scrud.android.generate

import com.github.scrud.android.view.AndroidResourceAnalyzer._
import com.github.triangle.{PortableField, FieldList, BaseField}
import com.github.scrud.android.persistence.CursorField
import com.github.scrud.android.view._
import xml.NodeSeq
import com.github.scrud.android.{AndroidPlatformDriver, NamingConventions}
import com.github.scrud.{EntityName, CrudApplication, EntityType}

case class EntityFieldInfo(field: BaseField, rIdClasses: Seq[Class[_]], entityName: EntityName, application: CrudApplication) {
  private lazy val updateablePersistedFields = CursorField.updateablePersistedFields(field, rIdClasses)
  private val fieldPrefix = AndroidPlatformDriver.fieldPrefix(entityName)

  private def viewFields(field: BaseField): Seq[ViewField[_]] = field.deepCollect {
    case matchingField: ViewField[_] => matchingField
  }

  lazy val viewIdFieldInfos: Seq[ViewIdFieldInfo] = field.deepCollect {
    case viewIdField: ViewIdField[_] =>
      val idString = viewIdField.viewRef.fieldName(rIdClasses)
      val displayName = FieldLayout.toDisplayName(idString.stripPrefix(fieldPrefix))
      ViewIdFieldInfo(idString, displayName, viewIdField)
  }

  lazy val isDisplayable: Boolean = !displayableViewIdFieldInfos.isEmpty
  lazy val isPersisted: Boolean = !updateablePersistedFields.isEmpty
  lazy val isUpdateable: Boolean = isDisplayable && isPersisted

  lazy val nestedEntityTypeViewInfos: Seq[EntityTypeViewInfo] = field.deepCollect {
    case entityView: EntityView => EntityTypeViewInfo(application.entityType(entityView.entityName), application)
  }

  private[generate] lazy val shallowDisplayableViewIdFieldInfos: Seq[ViewIdFieldInfo] = viewIdFieldInfos.filter(_.layout.displayXml != NodeSeq.Empty)

  lazy val displayableViewIdFieldInfos: Seq[ViewIdFieldInfo] =
    shallowDisplayableViewIdFieldInfos ++ nestedEntityTypeViewInfos.flatMap(_.shallowDisplayableViewIdFieldInfos)

  lazy val updateableViewIdFieldInfos: Seq[ViewIdFieldInfo] =
    if (isPersisted) viewIdFieldInfos.filter(_.layout.editXml != NodeSeq.Empty) else Nil

  lazy val otherViewFields = {
    val viewFieldsWithinViewIdFields = viewFields(FieldList(viewIdFieldInfos.map(_.field):_*))
    viewFields(field).filterNot(viewFieldsWithinViewIdFields.contains)
  }
}

case class ViewIdFieldInfo(id: String, displayName: String, field: PortableField[_]) {
  lazy val viewFields: Seq[ViewField[_]] = field.deepCollect {
    case matchingField: ViewField[_] => matchingField
  }

  lazy val layout: FieldLayout = viewFields.headOption.map(_.defaultLayout).getOrElse(FieldLayout.noLayout)
}

object ViewIdFieldInfo {
  def apply(id: String, viewField: PortableField[_]): ViewIdFieldInfo =
    ViewIdFieldInfo(id, FieldLayout.toDisplayName(id), viewField)
}

case class EntityTypeViewInfo(entityType: EntityType, application: CrudApplication) {
  val entityName = entityType.entityName
  lazy val layoutPrefix = NamingConventions.toLayoutPrefix(entityType.entityName)
  lazy val rIdClasses: Seq[Class[_]] = detectRIdClasses(entityType.getClass)
  lazy val entityFieldInfos: List[EntityFieldInfo] = entityType.fields.map(EntityFieldInfo(_, rIdClasses, entityName, application))
  private[generate] lazy val shallowDisplayableViewIdFieldInfos: List[ViewIdFieldInfo] = entityFieldInfos.flatMap(_.shallowDisplayableViewIdFieldInfos)
  lazy val displayableViewIdFieldInfos: List[ViewIdFieldInfo] = entityFieldInfos.flatMap(_.displayableViewIdFieldInfos)
  lazy val shortDisplayableViewIdFieldInfos: List[ViewIdFieldInfo] =
    displayableViewIdFieldInfos.filter(!_.layout.editXml.toString().contains("textMultiLine"))
  lazy val identifyingDisplayableViewIdFieldInfos: List[ViewIdFieldInfo] = shortDisplayableViewIdFieldInfos.take(1)
  lazy val updateableViewIdFieldInfos: List[ViewIdFieldInfo] = entityFieldInfos.flatMap(_.updateableViewIdFieldInfos)
  lazy val isUpdateable: Boolean = !updateableViewIdFieldInfos.isEmpty
}
