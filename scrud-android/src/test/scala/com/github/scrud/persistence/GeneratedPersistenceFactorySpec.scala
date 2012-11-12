package com.github.scrud.persistence

import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import com.xtremelabs.robolectric.RobolectricTestRunner
import org.junit.Test
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.github.triangle.PortableField._
import com.github.scrud.platform.PlatformTypes._
import android.app.Activity
import android.view.LayoutInflater
import android.widget.{BaseAdapter, AdapterView, ListAdapter}
import com.github.triangle.GetterInput
import com.github.scrud.state.State
import com.github.scrud.{CrudContext, EntityType, EntityName, UriPath}
import com.github.scrud.util.{ListenerHolder, CrudMockitoSugar}
import com.github.scrud.android.{AndroidCrudContext, CrudListActivity}

/** A behavior specification for [[com.github.scrud.persistence.GeneratedPersistenceFactory]].
  * @author Eric Pabst (epabst@gmail.com)
  */

@RunWith(classOf[RobolectricTestRunner])
class GeneratedPersistenceFactorySpec extends MustMatchers with CrudMockitoSugar {
  val seqPersistence = mock[SeqCrudPersistence[Map[String,Any]]]
  val adapterView = mock[AdapterView[BaseAdapter]]
  val activity = mock[Activity]
  val listAdapterCapture = capturingAnswer[Unit] { Unit }
  val generatedEntityName = EntityName("Generated")
  val crudContext = mock[AndroidCrudContext]
  val layoutInflater = mock[LayoutInflater]
  val dataListenerHolder = mock[ListenerHolder[DataListener]]

  @Test
  def itsListAdapterMustGetTheItemIdUsingTheIdField() {
    val factory = new GeneratedPersistenceFactory[Map[String,Any]] {
      def createEntityPersistence(entityType: EntityType, crudContext: CrudContext) = seqPersistence
    }
    val entityType = new EntityType(generatedEntityName) {
      override protected val idField = mapField[ID]("longId") + super.idField
      def valueFields = Nil
    }
    stub(activity.getLayoutInflater).toReturn(layoutInflater)
    stub(crudContext.activityState).toReturn(new State {})
    stub(crudContext.applicationState).toReturn(new State {})
    stub(crudContext.dataListenerHolder(entityType)).toReturn(dataListenerHolder)
    when(adapterView.setAdapter(anyObject())).thenAnswer(listAdapterCapture)
    val persistence = mock[CrudPersistence]
    when(crudContext.openEntityPersistence(entityType)).thenReturn(persistence)
    val uri = UriPath.EMPTY
    when(persistence.findAll(uri)).thenReturn(List(Map("longId" -> 456L)))
    new CrudListActivity().setListAdapter(adapterView, entityType, uri, crudContext, GetterInput.empty, activity, 123)
    verify(adapterView).setAdapter(anyObject())
    val listAdapter = listAdapterCapture.params(0).asInstanceOf[ListAdapter]
    listAdapter.getItemId(0) must be (456L)
  }
}
