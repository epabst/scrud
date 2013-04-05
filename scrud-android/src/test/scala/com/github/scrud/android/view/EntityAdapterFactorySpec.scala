package com.github.scrud.android.view

import org.mockito.Mockito._
import org.mockito.Matchers._
import com.github.scrud.persistence.{SimpleRefreshableFindAll, CrudPersistence}
import org.scalatest.matchers.MustMatchers
import com.github.scrud.util.CrudMockitoSugar
import com.github.scrud.android._
import com.github.scrud.{SimpleCrudContext, UriPath, CrudContextItems}
import org.junit.runner.RunWith
import org.junit.Test
import com.github.scrud.platform.PlatformTypes
import com.github.triangle.PortableField._
import com.github.scrud.platform.PlatformTypes._
import android.widget.BaseAdapter

/**
 * A behavior specification for [[com.github.scrud.android.view.EntityAdapterFactory]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/11/12
 * Time: 3:18 PM
 */
@RunWith(classOf[CustomRobolectricTestRunner])
class EntityAdapterFactorySpec extends MustMatchers with CrudMockitoSugar {
  @Test
  def itMustCreateAnAdapterSuccessfully() {
    val persistence = mock[CrudPersistence]
    val itemViewInflater = mock[ViewInflater]
    val contextItems = new CrudContextItems(UriPath("/uri"), null)
    val entityType = EntityTypeForTesting
    when(persistence.entityType).thenReturn(entityType)
    when(persistence.findAll(any())).thenReturn(Nil)

    val factory = new EntityAdapterFactory()
    val adapter = factory.createAdapter(persistence, contextItems, itemViewInflater)
    verify(persistence, times(1)).findAll(any())
    verify(persistence, never()).find(any())
    verify(persistence, never()).find(any[PlatformTypes.ID](), any[UriPath]())
    adapter must not be (null)
  }

  @Test
  def itMustGetTheItemIdUsingTheIdField() {
    val entityType = new EntityTypeForTesting() {
      override protected val idField = mapField[ID]("longId") + super.idField
      override def valueFields = Nil
    }
    val uri = UriPath.EMPTY
    val persistence = mock[CrudPersistence]
    when(persistence.entityType).thenReturn(entityType)
    when(persistence.findAll(uri)).thenReturn(List(Map("longId" -> 456L)))
    val refreshableFindAll = new SimpleRefreshableFindAll(uri, persistence)
    when(persistence.refreshableFindAll(uri)).thenReturn(refreshableFindAll)
    val _crudApplication = new CrudApplicationForTesting(entityType -> new PersistenceFactoryForTesting(persistence))
    val crudContext = new SimpleCrudContext(_crudApplication)
    val itemViewInflater = mock[ViewInflater]
    val contextItems = new CrudContextItems(uri, crudContext)
    val factory = new EntityAdapterFactory()
    val adapter = factory.createAdapter(persistence, contextItems, itemViewInflater).asInstanceOf[BaseAdapter]
    adapter.getItemId(0) must be (456L)
  }
}
