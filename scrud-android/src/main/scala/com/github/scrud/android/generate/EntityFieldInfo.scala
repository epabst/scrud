package com.github.scrud.android.generate

import com.github.scrud.android.view._
import scala.xml.{Elem, MetaData, Node, NodeSeq}
import com.github.scrud.{EntityName, BaseFieldDeclaration}
import com.github.scrud.platform.representation.PersistenceRange
import com.github.scrud.persistence.EntityTypeMap
import com.github.scrud.copy.{NestedTargetField, TargetType, TargetField}
import com.github.scrud.util.Name

case class EntityFieldInfo(field: BaseFieldDeclaration, rIdClasses: Seq[Class[_]], entityTypeMap: EntityTypeMap) {
  val entityName = field.entityName

  lazy val isPersisted: Boolean = field.representations.exists(_.isInstanceOf[PersistenceRange])

  def findTargetedFieldInfo(targetType: TargetType, fieldPrefix: String = ""): Option[TargetedFieldInfo[Nothing]] =
    field.toAdaptableField.findTargetField(targetType).map(TargetedFieldInfo[Nothing](_, this, targetType, fieldPrefix))

  def targetedFieldInfoOrFail(targetType: TargetType, fieldPrefix: String = ""): TargetedFieldInfo[Nothing] =
    findTargetedFieldInfo(targetType, fieldPrefix).getOrElse(sys.error(field + " does not have targetType=" + targetType))

  lazy val nestedEntityTypeViewInfoOpt: Option[EntityTypeViewInfo] = field.qualifiedType match {
    case referencedEntityName: EntityName => Some(EntityTypeViewInfo(entityTypeMap.entityType(referencedEntityName), entityTypeMap))
    case _ => None
  }
}

case class ViewIdFieldInfo(id: String, displayName: String, field: BaseFieldDeclaration, targetField: TargetField[Nothing], entityTypeMap: EntityTypeMap) {
  def this(id: String, field: BaseFieldDeclaration, targetField: TargetField[Nothing], entityTypeMap: EntityTypeMap) {
    this(id, Name(id).toDisplayableString, field, targetField, entityTypeMap)
  }

  private val defaultLayoutOpt: Option[NodeSeq] = findLayout(targetField)

  private def findLayout[V](targetField: TargetField[V]): Option[NodeSeq] = targetField match {
    case viewTargetField: TypedViewTargetField[_,_] => Some(viewTargetField.defaultLayout)
    case nestedTargetField: NestedTargetField[_] => findLayout(nestedTargetField.nestedField)
    case _ => None
  }

  val defaultLayoutOrEmpty = defaultLayoutOpt.getOrElse(NodeSeq.Empty)

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

  private def adjustHeadNode(xml: NodeSeq, f: Node => Node): NodeSeq = xml.headOption.fold(xml)(f(_) +: xml.tail)
}

object ViewIdFieldInfo {
  def apply(id: String, field: BaseFieldDeclaration, targetField: TargetField[Nothing], entityTypeMap: EntityTypeMap): ViewIdFieldInfo =
    ViewIdFieldInfo(id, Name(id).toDisplayableString, field, targetField, entityTypeMap)
}
