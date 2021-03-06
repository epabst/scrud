package com.github.scrud.types

import org.scalatest.FunSpec
import com.github.scrud.EntityName
import org.scalatest.matchers.MustMatchers
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * A behavior specification for [[com.github.scrud.types.EntityMultiSelectQT]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 1/31/14
 *         Time: 3:22 PM
 */
@RunWith(classOf[JUnitRunner])
class EntityMultiSelectQTSpec extends FunSpec with MustMatchers {
  val qualifiedType = EntityMultiSelectQT(EntityName("Foo"))
  
  it("must convert a single ID to/from a String") {
    val string = qualifiedType.convertToDisplayString(Set(45L))
    qualifiedType.convertFromString(string).get must be (Set(45L))
  }
  
  it("must convert an empty Set to/from a String") {
    val string = qualifiedType.convertToDisplayString(Set.empty)
    qualifiedType.convertFromString(string).get must be (Set.empty)
  }

  it("must convert multiple ID's to/from a String") {
    val string = qualifiedType.convertToDisplayString(Set(45L, 23L, 99L))
    qualifiedType.convertFromString(string).get must be (Set(45L, 23L, 99L))
  }

  describe("convertToString") {
    it("must format the String such that a search for :ID: works (where : is the delimiter)") {
      val idSet = Set(45L, 1000L, 23L, 99L, 3L)
      val string = qualifiedType.convertToDisplayString(idSet)
      string must startWith(EntityMultiSelectQT.delimiter)
      string must endWith(EntityMultiSelectQT.delimiter)
      string.count(_.toString == EntityMultiSelectQT.delimiter) must be (idSet.size + 1)
    }
    
    it("must format the String such that the ID's are sorted by ID") {
      val idSet = Set(45L, 1000L, 23L, 99L, 3L)
      val string = qualifiedType.convertToDisplayString(idSet)
      val sortedIds = idSet.toSeq.sorted
      val indices = sortedIds.map(id => string.indexOf(qualifiedType.convertToDisplayString(Set(id))))
      indices must be (indices.sorted)
    }

    it("the entries in the formatted String should sort the same as the ID's themselves (assuming they have the same # of characters)") {
      val idSet = Set(15L, 23L, 11L, 32L)
      val sortedIds = idSet.toSeq.sorted
      val sortedIdStringsAsString = idSet.map(id => qualifiedType.convertToDisplayString(Set(id))).toSeq.sorted.mkString
      val indices = sortedIds.map(id => sortedIdStringsAsString.indexOf(qualifiedType.convertToDisplayString(Set(id))))
      indices must be (indices.sorted)
    }

    it("must format the String using Base 64 to minimize string length") {
      val string: String = qualifiedType.convertIdToString(100000000000L)
      string.length must be <= 7
    }
  }
}
