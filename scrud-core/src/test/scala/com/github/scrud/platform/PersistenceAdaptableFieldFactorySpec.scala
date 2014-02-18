package com.github.scrud.platform

import org.scalatest.{MustMatchers, FunSpec}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import com.github.scrud.{EntityName, EntityType}
import com.github.scrud.types.TitleQT
import com.github.scrud.platform.representation.Persistence
import com.github.scrud.copy.types.{MapStorage, Default}
import com.github.scrud.copy.SourceType
import com.github.scrud.context.RequestContextForTesting

/**
 * A Behavior specification for [[com.github.scrud.platform.PersistenceAdaptableFieldFactory]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 2/17/14
 *         Time: 9:01 PM
 */
@RunWith(classOf[JUnitRunner])
class PersistenceAdaptableFieldFactorySpec extends FunSpec with MustMatchers {
  describe("Persistence(version)") {
    it("must distinguish between data versions") {
      val platformDriver = TestingPlatformDriver
      val entityName = EntityName("Foo")
      val entityType = new EntityType(entityName, platformDriver) {
        field("bar1-2", TitleQT, Seq(Persistence(1, 2), Default("bar1-2")))
        field("bar1", TitleQT, Seq(Persistence(1), Default("bar1")))
        field("bar3", TitleQT, Seq(Persistence(3), Default("bar3")))
        field("bar4", TitleQT, Seq(Persistence(4), Default("bar4")))
        field("bar2-5", TitleQT, Seq(Persistence(2, 5), Default("bar2-5")))
      }
      val resultMapStorage = entityType.adapt(SourceType.none, Persistence(3)).copyAndUpdate(None, new MapStorage,
        new RequestContextForTesting(entityType))
      resultMapStorage must be (new MapStorage(entityName,
        "bar1" -> Some("bar1"),
        "bar3" -> Some("bar3"),
        "bar2-5" -> Some("bar2-5")
      ))
    }
  }
}
