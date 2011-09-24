package com.github.scala_android.crud

import action.{UriPath, Action}
import org.junit.runner.RunWith
import scala.collection.mutable
import org.scalatest.matchers.MustMatchers
import org.scalatest.Spec
import android.widget.ListAdapter
import android.content.Intent
import com.xtremelabs.robolectric.RobolectricTestRunner
import org.junit.Test
import android.app.ListActivity
import org.easymock.EasyMock._
import ParentField.foreignKey

/**
 * A behavior specification for {@link CrudType}.
 * @author Eric Pabst (epabst@gmail.com)
 * Date: 4/12/11
 * Time: 7:59 PM
 */

@RunWith(classOf[RobolectricTestRunner])
class GeneratedCrudTypeSpec extends Spec with MustMatchers with MyEntityTesting with CrudEasyMockSugar {

  @Test
  def itMustCreateListAdapterWithIntentUsedForCriteria() {
    val seqPersistence = mock[SeqCrudPersistence[mutable.Map[String,Any]]]
    val crudContext = mock[CrudContext]
    val activity = mock[CrudListActivity]
    val listAdapterCapture = capturingAnswer[Unit] { Unit }
    val otherType = new MyEntityType(seqPersistence, mock[ListAdapter])
    val foreign = foreignKey(otherType)
    val generatedType = new GeneratedCrudType[mutable.Map[String,Any]] with StubEntityType {
      def entityName = "Generated"
      def valueFields = List(foreign)
      def openEntityPersistence(crudContext: CrudContext) = seqPersistence
    }
    expecting {
      val uri = UriPath(otherType.entityName, "123")
      call(activity.currentUriPath).andReturn(uri)
      call(seqPersistence.findAll(uri)).andReturn(List.empty)
      call(activity.setListAdapter(notNull())).andAnswer(listAdapterCapture)
      call(seqPersistence.entityType).andStubReturn(generatedType)
    }
    whenExecuting(seqPersistence, crudContext, activity) {
      generatedType.setListAdapter(seqPersistence, crudContext, activity)
      val listAdapter = listAdapterCapture.params(0).asInstanceOf[ListAdapter]
      listAdapter.getCount must be (0)
    }
  }
}