package com.github.scrud.android.view

import com.github.scrud.android.AndroidPlatformDriver
import com.github.scrud.platform.AdaptableFieldFactory
import com.github.scrud.types._
import com.github.scrud.copy._
import com.github.scrud.platform.representation.DisplayUI
import com.github.scrud.{FieldName, EntityName}
import android.view.View

/**
 * An AdaptableFieldFactory for the Android UI. 
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 4/26/14
 *         Time: 6:59 AM
 */
class AndroidDisplayUIFieldFactory(platformDriver: AndroidPlatformDriver) extends AdaptableFieldFactory {
  /**
   * Turns some sequence of [[com.github.scrud.copy.Representation]]s
   * into an [[com.github.scrud.copy.AdaptableField]].
   * Any Representations not included in the result are often processed by other factories.
   * @param entityName the name of the entity that contains the field
   * @param fieldName the name of the field in the entity
   * @param qualifiedType the type of the field in the entity
   * @param representations the Representations to consider adapting to
   * @tparam V the type of the field's value
   * @return the field and the representations it adapts to
   */
  def adapt[V](entityName: EntityName, fieldName: FieldName, qualifiedType: QualifiedType[V],
               representations: Seq[Representation[V]]): AdaptableFieldWithRepresentations[V] = {
    val displayUIs = representations.collect { case displayUI: DisplayUI => displayUI }
    if (!displayUIs.isEmpty) {
      val targetField: ViewTargetField[_ <: View,V] = qualifiedType match {
        case stringConvertibleType: StringConvertibleQT[V] => new TextViewField[V](stringConvertibleType, defaultTextViewLayout)
        case referencedEntityName @ EntityName(_) => new EntityReferenceView(referencedEntityName).asInstanceOf[ViewTargetField[_ <: View,V]]
      }
      val viewSpecifier = platformDriver.toViewSpecifier(entityName, "", fieldName)
      AdaptableFieldWithRepresentations(AdaptableField(Seq.empty,
        displayUIs.map(_ -> targetField.forTargetView(viewSpecifier))), displayUIs.toSet)
    } else {
      AdaptableFieldWithRepresentations.empty
    }
  }

  val defaultTextViewLayout = <TextView style="@android:style/TextAppearance.Widget.TextView"/>
}
