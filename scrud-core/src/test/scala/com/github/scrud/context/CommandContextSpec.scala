package com.github.scrud.context

import com.github.scrud.{EntityTypeForTesting, UriPath}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{MustMatchers, FunSpec}
import org.scalatest.mock.MockitoSugar
import com.github.scrud.copy.types.MapStorage
import java.util.Date

/**
 * A behavior specification for [[com.github.scrud.context.CommandContext]].
 * @author Eric Pabst (epabst@gmail.com)
 */
@RunWith(classOf[JUnitRunner])
class CommandContextSpec extends FunSpec with MustMatchers with MockitoSugar {

  describe("saveIfValid") {
    val entityType = new EntityTypeForTesting

    it("should support adding (without finding)") {
      val commandContext = new CommandContextForTesting(entityType)
      val birthDate = new Date()
      val entity = new MapStorage(entityType.name -> Some("Bob"), entityType.birthDate -> Some(birthDate))
      val uri = UriPath(entityType.entityName)
      val idOpt = commandContext.saveIfValid(uri, MapStorage, entity, entityType)
      idOpt must be ('defined)
      val savedEntity = commandContext.find(entityType.toUri(idOpt), MapStorage).get
      savedEntity.get(entityType.name) must be (Some("Bob"))
      savedEntity.get(entityType.birthDate) must be (Some(birthDate))
    }

    it("should support updating") {
      val commandContext = new CommandContextForTesting(entityType)
      val birthDate = new Date()
      val entity = new MapStorage(entityType.name -> Some("Bobby"), entityType.birthDate -> Some(birthDate))
      val id = commandContext.save(entityType.entityName, MapStorage, None, entity)
      commandContext.find(entityType.toUri(id), entityType.name) must be (Some("Bobby"))

      val updatedEntity = new MapStorage(entityType.name -> Some("Bob"), entityType.birthDate -> Some(birthDate))
      val uri = entityType.toUri(id)
      val idOpt = commandContext.saveIfValid(uri, MapStorage, updatedEntity, entityType)
      idOpt must be (Some(id))
      commandContext.find(entityType.toUri(id), entityType.name) must be (Some("Bob"))
    }
  }
}
