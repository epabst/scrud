package com.github.scrud.android.view

import com.github.scrud.android.AndroidPlatformDriver
import com.github.scrud.platform.AdaptableFieldFactory
import com.github.scrud.types._
import com.github.scrud.copy._
import com.github.scrud.platform.representation.EditUI
import com.github.scrud.{FieldName, EntityName}
import com.github.scrud.types.EnumerationValueQT

/**
 * An AdaptableFieldFactory for the Android UI. 
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 4/26/14
 *         Time: 6:59 AM
 */
class AndroidEditUIFieldFactory(platformDriver: AndroidPlatformDriver) extends AdaptableFieldFactory {
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
    if (representations.contains(EditUI)) {
      val childViewField = qualifiedType match {
        case qType@TitleQT => new EditTextField(qType, editTextLayout("text|textCapWords|textAutoCorrect"))
        case qType@DescriptionQT => new EditTextField(qType, editTextLayout("text|textCapSentences|textMultiLine|textAutoCorrect"))
        case qType@CurrencyQT => new EditTextField(qType, editTextLayout("numberDecimal|numberSigned"))
        case qType@PercentageQT => new EditTextField(qType, editTextLayout("numberDecimal|numberSigned"))
        case DateWithoutTimeQT => new DatePickerField()
        case EnumerationValueQT(enumeration) => EnumerationView(enumeration)
        case qType@EntityName(_) => SelectEntityView(qType)
        case qType: StringQualifiedType => new EditTextField(qType, editTextLayout("text|textAutoCorrect"))
        case qType: IntQualifiedType => new EditTextField(qType, editTextLayout("number"))
      }
      val viewSpecifier = platformDriver.toViewSpecifier(entityName, "edit_", fieldName)
      AdaptableFieldWithRepresentations(AdaptableField(
        Seq(EditUI -> childViewField.forSourceView(viewSpecifier)),
        Seq(EditUI -> childViewField.forTargetView(viewSpecifier))), Set(EditUI))
      //todo include supporting OperationResponse where the requestId == viewId
    } else {
      AdaptableFieldWithRepresentations.empty
    }
  }

  private def editTextLayout(inputType: String) = <EditText android:inputType={inputType}/>
}
