package com.github.scrud.android

import _root_.android.content.{ContentValues, Intent}
import com.github.scrud.android.action.StartActivityOperation
import com.github.scrud._
import org.junit.Test
import org.junit.runner.RunWith
import com.github.scrud.android.action.AndroidOperation.toRichItent
import com.github.scrud.types._
import com.github.scrud.platform.representation.{Query, Persistence, DetailUI}
import com.github.scrud.persistence.EntityTypeMapForTesting
import _root_.android.database.Cursor
import org.mockito.Mockito._
import com.github.scrud.android.persistence.ContentValuesStorage
import com.github.scrud.copy.SourceType
import com.github.scrud.copy.types.MapStorage
import com.github.scrud.FieldName
import com.github.scrud.EntityName
import scala.Some
import com.github.scrud.action.OperationAction
import com.github.scrud.types.EnumerationValueQT
import com.github.scrud.android.persistence.SQLiteCriteria
import org.robolectric.annotation.Config

/** A test for [[com.github.scrud.android.AndroidPlatformDriver]].
  * @author Eric Pabst (epabst@gmail.com)
  */
//todo make contract tests run as well as JUnit tests.
@RunWith(classOf[CustomRobolectricTestRunner])
@Config(manifest = "target/generated/AndroidManifest.xml")
class AndroidPlatformDriverSpec extends ScrudRobolectricSpecBase {
  //todo determine if shadowing, and run tests on real Android device as well.
  val isShadowing = true
  val driver = AndroidPlatformDriverForTesting
  val entityNavigation = new EntityNavigationForTesting(new EntityTypeMapForTesting(EntityTypeForTesting)) {
    /** Return true if the entity may be displayed in a mode that is distinct from editing. */
    override protected def isDisplayableWithoutEditing(entityName: EntityName): Boolean = true
  }
  val application = new CrudAndroidApplication(entityNavigation)

  import EntityTypeForTesting.entityName

  protected def makePlatformDriver() = driver

  val OperationAction(_, createOperation: StartActivityOperation) = entityNavigation.actionsToCreate(EntityTypeForTesting.entityName).head
  val OperationAction(_, listOperation: StartActivityOperation) = entityNavigation.actionsToList(EntityTypeForTesting.entityName).head
  val OperationAction(_, displayOperation: StartActivityOperation) = entityNavigation.actionsToDisplay(EntityTypeForTesting.entityName).head
  val OperationAction(_, updateOperation: StartActivityOperation) = entityNavigation.actionsToUpdate(EntityTypeForTesting.entityName).head

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
    val field = driver.field(EntityName("Bar"), FieldName("foo"), qualifiedType, Seq(DetailUI))
    field.toAdaptableField.findTargetField(DetailUI) must be ('isDefined)
  }

  @Test
  def persistenceShouldReturnNoneIfNullInCursor() {
    val cursor = mock[Cursor]
    when(cursor.getColumnIndex("name")).thenReturn(1)
    when(cursor.isNull(1)).thenReturn(true)
    val commandContext = new AndroidCommandContextForTesting(EntityTypeForTesting)
    EntityTypeForTesting.name.findApplicable(Persistence.Latest, cursor, UriPath.EMPTY, commandContext) must be (None)
  }

  @Test
  def persistenceFieldShouldNotBeDefinedIfColumnNotInCursor() {
    val cursor = mock[Cursor]
    when(cursor.getColumnIndex("name")).thenReturn(-1)
    val commandContext = new AndroidCommandContextForTesting(EntityTypeForTesting)
    val exception = intercept[IllegalArgumentException] {
      EntityTypeForTesting.name.findApplicable(Persistence.Latest, cursor, UriPath.EMPTY, commandContext)
    }
    exception.getMessage must include ("not")
    exception.getMessage must include ("name")
  }

  @Test
  def persistenceShouldReturnNoneIfNullInContentValues() {
    val contentValues = mock[ContentValues]
    when(contentValues.getAsString("name")).thenReturn(null)
    val commandContext = new AndroidCommandContextForTesting(EntityTypeForTesting)
    EntityTypeForTesting.name.findApplicable(ContentValuesStorage, contentValues, UriPath.EMPTY, commandContext) must be (None)
  }

  @Test
  def persistenceShouldPutNullIntoContentValuesForNoValue() {
    val contentValues = mock[ContentValues]
    val commandContext = new AndroidCommandContextForTesting(EntityTypeForTesting)
    EntityTypeForTesting.name.updateWithValue(Persistence.Latest, contentValues, None, UriPath.EMPTY, commandContext)
    verify(contentValues).putNull("name")
  }

  @Test
  def persistenceShouldNotPutAnythingIntoContentValuesForUndefined() {
    val contentValues = mock[ContentValues]
    val commandContext = new AndroidCommandContextForTesting(EntityTypeForTesting)
    val unknownSourceType = new SourceType {}
    try {
      EntityTypeForTesting.copyAndUpdate(unknownSourceType, new Object, UriPath.EMPTY, ContentValuesStorage, contentValues, commandContext)
    } catch {
      case e: UnsupportedOperationException if Option(e.getMessage).exists(_.contains(unknownSourceType.toString)) => Unit
    }
    verify(contentValues, atMost(1)).putNull(EntityTypeForTesting.id.fieldName.toSnakeCase)
    verifyNoMoreInteractions(contentValues)
  }

  @Test
  def shouldGetCriteriaCorrectlyForANumber() {
    val commandContext = new AndroidCommandContextForTesting(EntityTypeForTesting)
    val source = new MapStorage(EntityTypeForTesting.age -> Some(19))
    val criteria: SQLiteCriteria = EntityTypeForTesting.copyAndUpdate(
      MapStorage, source, UriPath.EMPTY, Query, new SQLiteCriteria(), commandContext)
    criteria.selection must be (List("age=19"))
  }

  @Test
  def shouldGetCriteriaCorrectlyForAString() {
    val commandContext = new AndroidCommandContextForTesting(EntityTypeForTesting)
    val source = new MapStorage(EntityTypeForTesting.name -> Some("John Doe"))
    val criteria: SQLiteCriteria = EntityTypeForTesting.copyAndUpdate(
      MapStorage, source, UriPath.EMPTY, Query, new SQLiteCriteria(), commandContext)
    criteria.selection must be (List("name=\"John Doe\""))
  }

  @Test
  def shouldHandleMultipleSelectionCriteria() {
    val commandContext = new AndroidCommandContextForTesting(EntityTypeForTesting)
    val source = new MapStorage(EntityTypeForTesting.name -> Some("John Doe"), EntityTypeForTesting.age -> Some(19))
    val criteria: SQLiteCriteria = EntityTypeForTesting.copyAndUpdate(
      MapStorage, source, UriPath.EMPTY, Query, new SQLiteCriteria(), commandContext)
    criteria.selection.toSet must be (Set("name=\"John Doe\"", "age=19"))
  }
}
