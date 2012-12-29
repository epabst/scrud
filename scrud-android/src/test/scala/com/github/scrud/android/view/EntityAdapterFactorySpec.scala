package com.github.scrud.android.view

import org.mockito.Mockito._
import org.mockito.Matchers._
import com.github.scrud.persistence.CrudPersistence
import org.scalatest.matchers.MustMatchers
import com.github.scrud.util.CrudMockitoSugar
import com.github.scrud.android.{CustomRobolectricTestRunner, MyEntityType}
import com.github.scrud.{UriPath, CrudContextItems}
import org.junit.runner.RunWith
import org.junit.Test
import com.github.scrud.platform.PlatformTypes

/**
 * A behavior specification for [[com.github.scrud.android.view.EntityAdapterFactory]].
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 12/11/12
 * Time: 3:18 PM
 */
@RunWith(classOf[CustomRobolectricTestRunner])
class EntityAdapterFactorySpec extends MustMatchers with CrudMockitoSugar {
  val entityType = MyEntityType

  @Test
  def itMustCreateAnAdapterSuccessfully() {
    val persistence = mock[CrudPersistence]
    val itemViewInflater = mock[ViewInflater]
    val contextItems = new CrudContextItems(UriPath("/uri"), null)
    when(persistence.entityType).thenReturn(entityType)

    val factory = new EntityAdapterFactory()
    val adapter = factory.createAdapter(persistence, contextItems, itemViewInflater)
    verify(persistence, times(1)).findAll(any())
    verify(persistence, never()).find(any())
    verify(persistence, never()).find(any[PlatformTypes.ID](), any[UriPath]())
    adapter must not be (null)
  }
}
