package com.github.scrud.android

import _root_.android.content.Intent
import action.StartActivityOperation
import com.github.scrud.UriPath
import org.junit.Test
import org.junit.runner.RunWith
import com.github.scrud.android.action.AndroidOperation.toRichItent
import com.github.scrud.util.CrudMockitoSugar
import com.github.scrud.types._
import com.github.scrud.EntityName
import com.github.scrud.action.OperationAction
import com.github.scrud.platform.representation.DetailUI
import org.scalatest.MustMatchers
import com.github.scrud.android.testres.R

/** A test for [[com.github.scrud.android.AndroidPlatformDriver]].
  * @author Eric Pabst (epabst@gmail.com)
  */
//todo make contract tests run as well as JUnit tests.
@RunWith(classOf[CustomRobolectricTestRunner])
class AndroidPlatformDriverSpec extends CrudMockitoSugar with MustMatchers {
  //todo determine if shadowing, and run tests on real Android device as well.
  val isShadowing = true
  val driver = new AndroidPlatformDriver(classOf[R])
  val application = new CrudApplicationForTesting(driver, CrudTypeForTesting) {
    override def hasDisplayPage(entityName: EntityName) = true
  }

  import EntityTypeForTesting.entityName

  protected def makePlatformDriver() = driver

  val OperationAction(_, createOperation: StartActivityOperation) = application.actionToCreate(EntityTypeForTesting).get
  val OperationAction(_, listOperation: StartActivityOperation) = application.actionToList(EntityTypeForTesting).get
  val OperationAction(_, displayOperation: StartActivityOperation) = application.actionToDisplay(EntityTypeForTesting).get
  val OperationAction(_, updateOperation: StartActivityOperation) = application.actionToUpdate(EntityTypeForTesting).get

  @Test
  def createActionShouldHaveTheRightUri() {
    val activity = new CrudActivityForTesting(application)
    createOperation.determineIntent(UriPath("foo"), activity).uriPath must
      be (UriPath("foo") / entityName)
    createOperation.determineIntent(UriPath("foo") / entityName, activity).uriPath must
      be (UriPath("foo") / entityName)
    createOperation.determineIntent(UriPath("foo").specify(entityName, 123), activity).uriPath must
      be (UriPath("foo") / entityName)
    createOperation.determineIntent(UriPath("foo").specify(entityName, 123).specify("bar"), activity).uriPath must
      be (UriPath("foo") / entityName)
    createOperation.determineIntent(UriPath(), activity).uriPath must
      be (UriPath(entityName))
  }

  @Test
  def listActionShouldHaveTheRightUri() {
    val activity = new CrudActivityForTesting(application)
    listOperation.determineIntent(UriPath("foo"), activity).uriPath must
      be (UriPath("foo") / entityName)
    listOperation.determineIntent(UriPath("foo", entityName.name), activity).uriPath must
      be (UriPath("foo") / entityName)
    listOperation.determineIntent(UriPath("foo", entityName.name, "123"), activity).uriPath must
      be (UriPath("foo") / entityName)
    listOperation.determineIntent(UriPath("foo", entityName.name, "123", "bar"), activity).uriPath must
      be (UriPath("foo") / entityName)
    listOperation.determineIntent(UriPath(), activity).uriPath must
      be (UriPath(entityName))
  }

  @Test
  def displayActionShouldHaveTheRightUri() {
    val activity = new CrudActivityForTesting(application)
    displayOperation.determineIntent(UriPath("foo", entityName.name, "35"), activity).uriPath must
      be (UriPath("foo", entityName.name, "35"))
    displayOperation.determineIntent(UriPath("foo", entityName.name, "34", "bar"), activity).uriPath must
      be (UriPath("foo", entityName.name, "34"))
    displayOperation.determineIntent(UriPath("foo", entityName.name, "34", "bar", "123"), activity).uriPath must
      be (UriPath("foo", entityName.name, "34"))
  }

  @Test
  def updateActionShouldHaveTheRightUri() {
    val activity = new CrudActivityForTesting(application)
    updateOperation.determineIntent(UriPath("foo", entityName.name, "35"), activity).uriPath must
      be (UriPath("foo", entityName.name, "35"))
    updateOperation.determineIntent(UriPath("foo", entityName.name, "34", "bar"), activity).uriPath must
      be (UriPath("foo", entityName.name, "34"))
    updateOperation.determineIntent(UriPath("foo", entityName.name, "34", "bar", "123"), activity).uriPath must
      be (UriPath("foo", entityName.name, "34"))
  }

  @Test
  def shouldHaveTheStandardActionNames() {
    if (!isShadowing) {
      val activity = null
      createOperation.determineIntent(UriPath("foo"), activity).getAction must be (Intent.ACTION_INSERT)
      listOperation.determineIntent(UriPath("foo"), activity).getAction must be (Intent.ACTION_PICK)
      displayOperation.determineIntent(UriPath("foo"), activity).getAction must be (Intent.ACTION_VIEW)
      updateOperation.determineIntent(UriPath("foo"), activity).getAction must be (Intent.ACTION_EDIT)
    }
  }

  @Test
  def shouldRecognizeQualifiedType_DateWithoutTimeQT() {
    assertQualifiedTypeRecognized(DateWithoutTimeQT)
  }

  @Test
  def shouldRecognizeQualifiedType_TitleQT() {
    assertQualifiedTypeRecognized(TitleQT)
    assertQualifiedTypeRecognized(DescriptionQT)
  }

  @Test
  def shouldRecognizeQualifiedType_NaturalIntQT() {
    assertQualifiedTypeRecognized(NaturalIntQT)
  }

  @Test
  def shouldRecognizeQualifiedType_PositiveIntQT() {
    assertQualifiedTypeRecognized(PositiveIntQT)
  }

  @Test
  def shouldRecognizeQualifiedType_PercentageQT() {
    assertQualifiedTypeRecognized(PercentageQT)
  }

  @Test
  def shouldRecognizeQualifiedType_CurrencyQT() {
    assertQualifiedTypeRecognized(CurrencyQT)
  }

  @Test
  def shouldRecognizeQualifiedType_EntityName() {
    assertQualifiedTypeRecognized(EntityName("Foo"))
  }

  @Test
  def shouldRecognizeQualifiedType_EnumerationValueQT() {
    object Genre extends Enumeration {
      val Fantasy = Value("Fantasy")
      val SciFi = Value("Sci-Fi")
    }
    assertQualifiedTypeRecognized(EnumerationValueQT(Genre))
  }

  def assertQualifiedTypeRecognized(qualifiedType: QualifiedType[_]) {
    driver.field(EntityName("Bar"), "foo", qualifiedType, Seq.empty).toAdaptableField.findTargetField(DetailUI) must be ('isDefined)
  }
}
