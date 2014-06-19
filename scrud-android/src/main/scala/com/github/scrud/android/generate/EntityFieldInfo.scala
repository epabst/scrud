package com.github.scrud.android.generate

import com.github.scrud.android.view._
import scala.xml.{Elem, MetaData, Node, NodeSeq}
import com.github.scrud.android.AndroidPlatformDriver
import com.github.scrud.{BaseFieldDeclaration, EntityName}
import com.github.scrud.platform.representation.{PersistenceRange, DetailUI, EditUI}
import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.copy.TargetField

case class EntityFieldInfo(field: BaseFieldDeclaration, rIdClasses: Seq[Class[_]], entityTypeMap: EntityTypeMap) {
  val entityName = field.entityName
  private val platformDriver = entityTypeMap.platformDriver.asInstanceOf[AndroidPlatformDriver]
  private val fieldPrefix = AndroidPlatformDriver.fieldPrefix(entityName)

  lazy val displayViewIdFieldInfos: Seq[ViewIdFieldInfo] = {
    field.toAdaptableField.findTargetField(DetailUI).toSeq.map { targetField =>
      val viewRef = platformDriver.toViewRef(entityName, "", field.fieldName)
      val idString = viewRef.fieldName(rIdClasses)
      val displayName = FieldLayout.toDisplayName(idString.stripPrefix(fieldPrefix))
      ViewIdFieldInfo(idString, displayName, field, targetField, entityTypeMap)
    }
  }

  lazy val editViewIdFieldInfos: Seq[ViewIdFieldInfo] = updateableViewIdFieldInfos

  lazy val isDisplayable: Boolean = !displayableViewIdFieldInfos.isEmpty
  lazy val isPersisted: Boolean = field.representations.exists(_.isInstanceOf[PersistenceRange])
  lazy val isUpdateable: Boolean = isDisplayable && isPersisted

  lazy val nestedEntityTypeViewInfoOpt: Option[EntityTypeViewInfo] = field.qualifiedType match {
    case referencedEntityName: EntityName => Some(EntityTypeViewInfo(entityTypeMap.entityType(referencedEntityName), entityTypeMap))
    case _ => None
  }

  private[generate] lazy val shallowDisplayableViewIdFieldInfos: Seq[ViewIdFieldInfo] = displayViewIdFieldInfos

  lazy val displayableViewIdFieldInfos: Seq[ViewIdFieldInfo] =
    shallowDisplayableViewIdFieldInfos ++ nestedEntityTypeViewInfoOpt.toSeq.flatMap(_.shallowDisplayableViewIdFieldInfos)

  lazy val updateableViewIdFieldInfos: Seq[ViewIdFieldInfo] = {
    field.toAdaptableField.findTargetField(EditUI).toSeq.map { targetField =>
      val viewRef = platformDriver.toViewRef(entityName, "edit_", field.fieldName)
      val idString = viewRef.fieldName(rIdClasses)
      val displayName = FieldLayout.toDisplayName(idString.stripPrefix(fieldPrefix))
      ViewIdFieldInfo(idString, displayName, field, targetField, entityTypeMap)
    }
  }
}

case class ViewIdFieldInfo(id: String, displayName: String, field: BaseFieldDeclaration, targetField: TargetField[Nothing], entityTypeMap: EntityTypeMap) {
  private val defaultLayoutOpt: Option[NodeSeq] = targetField match {
    case viewTargetField: TypedViewTargetField[_,_] => Some(viewTargetField.defaultLayout)
    case _ => None
  }

  val defaultLayoutOrEmpty = defaultLayoutOpt.getOrElse(NodeSeq.Empty)

  lazy val nestedEntityTypeViewInfoOpt: Option[EntityTypeViewInfo] = field.qualifiedType match {
    case referencedEntityName: EntityName => Some(EntityTypeViewInfo(entityTypeMap.entityType(referencedEntityName), entityTypeMap))
    case _ => None
  }

  def layoutForDisplayUI(position: Int): NodeSeq = {
    val textAppearance = if (position < 2) "?android:attr/textAppearanceLarge" else "?android:attr/textAppearanceSmall"
    val gravity = if (position % 2 == 0) "left" else "right"
    val layoutWidth = if (position % 2 == 0) "wrap_content" else "fill_parent"
    val attributes = <TextView android:id={"@+id/" + id} android:gravity={gravity}
                               android:layout_width={layoutWidth}
                               android:layout_height="wrap_content"
                               android:paddingRight="3sp"
                               android:textAppearance={textAppearance} style="@android:style/TextAppearance.Widget.TextView"/>.attributes
    defaultLayoutOpt.fold(NodeSeq.Empty)(adjustHeadNode(_, applyAttributes(_, attributes)))
  }

  def layoutForEditUI(position: Int): NodeSeq = {
    val textAppearance = "?android:attr/textAppearanceLarge"
    val attributes = <EditText android:id={"@+id/" + id} android:layout_width="fill_parent" android:layout_height="wrap_content"/>.attributes
    <result>
      <TextView android:text={displayName + ":"} android:textAppearance={textAppearance} style="@android:style/TextAppearance.Widget.TextView" android:layout_width="wrap_content" android:layout_height="wrap_content"/>
      {defaultLayoutOpt.fold(NodeSeq.Empty)(adjustHeadNode(_, applyAttributes(_, attributes)))}
    </result>.child
  }

  private def applyAttributes(xml: Node, attributes: MetaData): Node = xml match {
    case e: Elem => e % attributes
    case x => x
  }

  private def adjustHeadNode(xml: NodeSeq, f: Node => Node): NodeSeq = xml.headOption.map(f(_) +: xml.tail).getOrElse(xml)
}

object ViewIdFieldInfo {
  def apply(id: String, field: BaseFieldDeclaration, targetField: TargetField[Nothing], entityTypeMap: EntityTypeMap): ViewIdFieldInfo =
    ViewIdFieldInfo(id, FieldLayout.toDisplayName(id), field, targetField, entityTypeMap)
}
