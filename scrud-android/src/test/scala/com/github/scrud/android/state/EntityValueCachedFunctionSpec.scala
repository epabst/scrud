package com.github.scrud.android.state

import org.scalatest.FunSpec
import com.github.scrud.android.{CrudPersistence, MyEntityType, CrudContext}
import org.scalatest.mock.MockitoSugar
import com.github.scrud.android.common.UriPath
import org.scalatest.matchers.MustMatchers
import com.github.scrud.android.action.State
import org.mockito.Mockito._
import org.mockito.Matchers
import com.github.scrud.android.persistence.{CursorField, EntityType}
import org.mockito.stubbing.Answer
import org.mockito.invocation.InvocationOnMock

/**
 * A test for [[com.github.scrud.android.state.EntityValueCachedFunction]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 9/18/12
 *         Time: 3:26 PM
 */
class EntityValueCachedFunctionSpec extends FunSpec with MockitoSugar with MustMatchers {
  it("should get and cache results") {
    val uri = UriPath("a", "1", MyEntityType.entityName, "2")
    val entityData = Map("name" -> "George", "age" -> 45, "uri" -> "uri:george")
    val crudContext = mock[CrudContext]
    val input = (MyEntityType, uri, crudContext)

    when(crudContext.activityState).thenReturn(new State {})
    val persistence = mock[CrudPersistence]
    when(crudContext.withEntityPersistence_uncurried(Matchers.any[EntityType](), Matchers.any[CrudPersistence => Any]())).thenAnswer(new Answer[Any] {
      def answer(invocation: InvocationOnMock) = {
        val function = invocation.getArguments.apply(1).asInstanceOf[CrudPersistence => Any]
        function(persistence)
      }
    })
    when(persistence.find(uri)).thenReturn(Some(entityData))

    val result = EntityValueCachedFunction.apply(crudContext, input)
    result.get.update(Map.empty[String,Any]) must be (entityData + (CursorField.idFieldName -> 2))

    val result2 = EntityValueCachedFunction.apply(crudContext, input)
    result2.get.update(Map.empty[String,Any]) must be (entityData + (CursorField.idFieldName -> 2))
    // Should only use persistence once since it should be cached
    verify(crudContext, times(1)).withEntityPersistence_uncurried(Matchers.any[EntityType](), Matchers.any[CrudPersistence => Any]())
    // Should only use persistence once since it should be cached
    verify(persistence, times(1)).find(Matchers.any())
    // Should be a cache hit
    (result eq result2) must be (true)
  }
}
