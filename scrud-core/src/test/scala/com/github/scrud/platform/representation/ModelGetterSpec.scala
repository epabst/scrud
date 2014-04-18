package com.github.scrud.platform.representation

import org.scalatest.{MustMatchers, FunSpec}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import com.github.scrud.{UriPath, EntityTypeForTesting}
import com.github.scrud.copy.types.MapStorage
import com.github.scrud.context.CommandContextForTesting

/**
 * A behavior specification for [[com.github.scrud.platform.representation.ModelGetter]]. 
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/18/14
 *         Time: 9:16 AM
 */
@RunWith(classOf[JUnitRunner])
class ModelGetterSpec extends FunSpec with MustMatchers {
  it("must function") {
    val entityType = new EntityTypeForTesting {
      override protected def idFieldRepresentations = ModelGetter((person: Person) => person.myId) +: super.idFieldRepresentations
    }
    val commandContext = new CommandContextForTesting(entityType)
    val adaptedFields = entityType.adapt(EntityModel[Person], MapStorage)
    val copiedIdOpt = adaptedFields.copyAndUpdate(Person(Some(500L)), UriPath.EMPTY, new MapStorage(), commandContext).get(entityType.id)
    copiedIdOpt must be (Some(500L))
  }
  
  it("must not be used if the SourceType isn't EntityModel") {
    val entityType = new EntityTypeForTesting {
      override protected def idFieldRepresentations = ModelGetter((person: Person) => person.myId) +: super.idFieldRepresentations
    }
    val commandContext = new CommandContextForTesting(entityType)
    val adaptedFields = entityType.adapt(MapStorage, MapStorage)
    // must not throw an exception
    adaptedFields.copyAndUpdate(new MapStorage(), UriPath.EMPTY, new MapStorage(), commandContext).get(entityType.id)
  }

  case class Person(myId: Option[Long])
}
