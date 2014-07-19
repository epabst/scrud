package com.github.scrud.copy

import org.scalatest.{MustMatchers, FunSpec}
import com.github.scrud.{EntityType, EntityTypeForTesting}
import com.github.scrud.types.{TitleQT, NaturalIntQT}
import com.github.scrud.copy.types.MapStorage
import com.github.scrud.platform.representation.DetailUI
import java.util.{Date, Calendar, GregorianCalendar}
import com.github.scrud.context.CommandContextForTesting
import com.github.scrud.persistence.{PersistenceFactory, ListBufferPersistenceFactoryForTesting, EntityTypeMapForTesting}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * A behavior specification for [[com.github.scrud.copy.Derived]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/13/14
 *         Time: 10:06 AM
 */
@RunWith(classOf[JUnitRunner])
class DerivedSpec extends FunSpec with MustMatchers {
  it("must be aware of all of its fields") {
    val entityType = new EntityTypeForTesting {
      val derivedAge = Derived(birthDate) {
        case Some(birthDate) =>
          val todayCalendar = new GregorianCalendar()
          val birthDateCalendar = new GregorianCalendar()
          birthDateCalendar.setTime(birthDate)
          Some (todayCalendar.get(Calendar.YEAR) - birthDateCalendar.get(Calendar.YEAR))
        case _ => None
      }
      val AgeInYears = field("ageInYears", NaturalIntQT, Seq(MapStorage, DetailUI, derivedAge))

      val derivedNickname = Derived(name, AgeInYears) {
        case (Some(name), Some(age)) => Some(name + age)
        case _ => None
      }
      val Nickname = field("nickname", TitleQT, Seq(MapStorage, DetailUI, derivedNickname))
      val Nickname2 = field("nickname2", TitleQT, Seq(Derived(name, AgeInYears, Nickname) {
        case (Some(name), Some(age), Some(nickname)) => Some(name + age + nickname)
        case _ => None
      }))
    }
    val entityName = entityType.entityName
    val commandContext = new CommandContextForTesting(EntityTypeMapForTesting(Map[EntityType,PersistenceFactory](
      entityType -> ListBufferPersistenceFactoryForTesting)))
    val id = commandContext.save(entityName, MapStorage, None, new MapStorage(
      entityType.name -> Some("George"), entityType.birthDate -> Some(new Date())))
    val result = commandContext.find(entityName.toUri(id), entityType.Nickname2)
    result must be (Some("George0George0"))
  }
}
