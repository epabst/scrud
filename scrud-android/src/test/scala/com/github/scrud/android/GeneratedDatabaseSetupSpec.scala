package com.github.scrud.android

import org.junit.{Before, Test}
import org.junit.runner.RunWith
import android.database.sqlite.SQLiteDatabase
import com.github.scrud.util.CrudMockitoSugar
import org.scalatest.matchers.MustMatchers
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.github.scrud.persistence.CrudPersistence
import com.github.scrud.{EntityName, EntityType}

/**
 * A behavior specification for [[com.github.scrud.android.GeneratedDatabaseSetup]].
 *
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 12/27/12
 *         Time: 8:34 AM
 */
@RunWith(classOf[CustomRobolectricTestRunner])
class GeneratedDatabaseSetupSpec extends CrudMockitoSugar with MustMatchers {
  val db = mock[SQLiteDatabase]
  val mockEntityType = mock[EntityType]
  val platformDriver = new AndroidPlatformDriver(null)
  val entityType = new EntityTypeForTesting(EntityName("Entity1"), platformDriver) {
    override def onCreateDatabase(lowLevelPersistence: CrudPersistence) {
      mockEntityType.onCreateDatabase(lowLevelPersistence)
    }
  }
  val mockEntityType2 = mock[EntityType]
  val entityType2 = new EntityTypeForTesting(EntityName("Entity2"), platformDriver) {
    override def onCreateDatabase(lowLevelPersistence: CrudPersistence) {
      mockEntityType2.onCreateDatabase(lowLevelPersistence)
    }
  }
  val persistenceFactory = SQLitePersistenceFactory
  val application = new CrudApplicationForTesting(platformDriver, CrudType(entityType, persistenceFactory), CrudType(entityType2, persistenceFactory))
  val sut = new GeneratedDatabaseSetup(new AndroidCrudContextForTesting(application), persistenceFactory)

  @Before
  def setUp() {
    reset(mockEntityType, db)
  }

  @Test
  def onCreate_mustCreateTables() {
    sut.onCreate(db)
    verify(db, times(1)).execSQL(contains("CREATE TABLE IF NOT EXISTS Entity1"))
    verify(db, times(1)).execSQL(contains("CREATE TABLE IF NOT EXISTS Entity2"))
  }

  @Test
  def onCreate_mustCallOnCreateForAllSQLiteEntities() {
    sut.onCreate(db)
    verify(mockEntityType, times(1)).onCreateDatabase(any())
    verify(mockEntityType2, times(1)).onCreateDatabase(any())
  }

  @Test
  def onUpgrade_mustCreateMissingTables() {
    sut.onUpgrade(db, 1, 2)
    verify(db, times(1)).execSQL(contains("CREATE TABLE IF NOT EXISTS Entity1"))
    verify(db, times(1)).execSQL(contains("CREATE TABLE IF NOT EXISTS Entity2"))
  }
}
