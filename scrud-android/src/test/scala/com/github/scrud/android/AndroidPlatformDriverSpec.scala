package com.github.scrud.android

import _root_.android.content.Intent
import action.StartActivityOperation
import com.github.scrud.UriPath
import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import com.github.scrud.android.action.AndroidOperation.toRichItent
import com.github.scrud.util.CrudMockitoSugar
import res.R
import com.github.scrud.android.view.{EntityView, EnumerationView, ViewField}
import com.github.scrud.types._
import com.github.scrud.EntityName
import com.github.scrud.action.Action

/** A test for [[com.github.scrud.android.AndroidPlatformDriver]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[CustomRobolectricTestRunner])
class AndroidPlatformDriverSpec extends MustMatchers with CrudMockitoSugar {
  //todo determine if shadowing, and run tests on real Android device as well.
  val isShadowing = true
  val driver = new AndroidPlatformDriver(classOf[R])
  val application = new CrudApplicationForTesting(driver, CrudTypeForTesting) {
    override def hasDisplayPage(entityName: EntityName) = true
  }

  import EntityTypeForTesting.entityName

  val Action(_, createOperation: StartActivityOperation) = application.actionToCreate(EntityTypeForTesting).get
  val Action(_, listOperation: StartActivityOperation) = application.actionToList(EntityTypeForTesting).get
  val Action(_, displayOperation: StartActivityOperation) = application.actionToDisplay(EntityTypeForTesting).get
  val Action(_, updateOperation: StartActivityOperation) = application.actionToUpdate(EntityTypeForTesting).get

  @Test
  def createActionShouldHaveTheRightUri() {
    val activity = null
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
    val activity = null
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
    val activity = null
    displayOperation.determineIntent(UriPath("foo", entityName.name, "35"), activity).uriPath must
      be (UriPath("foo", entityName.name, "35"))
    displayOperation.determineIntent(UriPath("foo", entityName.name, "34", "bar"), activity).uriPath must
      be (UriPath("foo", entityName.name, "34"))
    displayOperation.determineIntent(UriPath("foo", entityName.name, "34", "bar", "123"), activity).uriPath must
      be (UriPath("foo", entityName.name, "34"))
  }

  @Test
  def updateActionShouldHaveTheRightUri() {
    val activity = null
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
    assertQualifiedTypeRecognized(DateWithoutTimeQT, ViewField.dateView)
  }

  @Test
  def shouldRecognizeQualifiedType_TitleQT() {
    assertQualifiedTypeRecognized(TitleQT, ViewField.textView)
    assertQualifiedTypeRecognized(DescriptionQT, ViewField.textView)
  }

  @Test
  def shouldRecognizeQualifiedType_NaturalIntQT() {
    assertQualifiedTypeRecognized(NaturalIntQT, ViewField.intView)
  }

  @Test
  def shouldRecognizeQualifiedType_PositiveIntQT() {
    assertQualifiedTypeRecognized(PositiveIntQT, ViewField.intView)
  }

  @Test
  def shouldRecognizeQualifiedType_PercentageQT() {
    assertQualifiedTypeRecognized(PercentageQT, ViewField.percentageView)
  }

  @Test
  def shouldRecognizeQualifiedType_CurrencyQT() {
    assertQualifiedTypeRecognized(CurrencyQT, ViewField.currencyView)
  }

  @Test
  def shouldRecognizeQualifiedType_EntityName() {
    assertQualifiedTypeRecognized(EntityName("Foo"), EntityView(EntityName("Foo")))
  }

  @Test
  def shouldRecognizeQualifiedType_EnumerationValueQT() {
    object Genre extends Enumeration {
      val Fantasy = Value("Fantasy")
      val SciFi = Value("Sci-Fi")
    }
    assertQualifiedTypeRecognized(EnumerationValueQT(Genre), EnumerationView(Genre))
  }

  def assertQualifiedTypeRecognized(qualifiedType: QualifiedType[_], expectedField: ViewField[_]) {
    driver.namedViewField("foo", qualifiedType, EntityName("Bar")).deepCollect {
      case view if view == expectedField => view
    }.size must be(1)
  }
}