package com.github.scrud.android.view

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers
import com.github.scrud.android.testres._
import com.github.scrud.android.R

/** A behavior specification for [[com.github.scrud.android.view.AndroidResourceAnalyzer]].
  * @author Eric Pabst (epabst@gmail.com)
  */
@RunWith(classOf[JUnitRunner])
class AndroidResourceAnalyzerSpec extends FunSpec with MustMatchers {
  describe("detectRIdClasses") {
    it("must be able to find all of the R.id instances") {
      AndroidResourceAnalyzer.detectRIdClasses(classOf[SiblingToR]) must
              be (Seq(classOf[R.id], classOf[android.R.id]))
    }

    it("must look in parent packages to find the application R.id instance") {
      AndroidResourceAnalyzer.detectRIdClasses(classOf[subpackage.ClassInSubpackage]) must
              be (Seq(classOf[R.id], classOf[android.R.id]))
    }
  }

  describe("detectRLayoutClasses") {
    it("must be able to find all of the R.layout instances") {
      AndroidResourceAnalyzer.detectRLayoutClasses(classOf[SiblingToR]) must
              be (Seq(classOf[R.layout], classOf[android.R.layout]))
    }

    it("must look in parent packages to find the application R.layout instance") {
      AndroidResourceAnalyzer.detectRLayoutClasses(classOf[subpackage.ClassInSubpackage]) must
              be (Seq(classOf[R.layout], classOf[android.R.layout]))
    }
  }

  it("must locate a resource field by name") {
    AndroidResourceAnalyzer.findResourceFieldWithName(List(classOf[R.id]), "foo").map(_.get(null)) must be (Some(123))
  }

  it("must locate a resource field by value") {
    AndroidResourceAnalyzer.findResourceFieldWithIntValue(List(classOf[R.id]), 123).map(_.getName) must be (Some("foo"))
  }
}
