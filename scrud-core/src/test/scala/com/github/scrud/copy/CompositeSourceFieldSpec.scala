package com.github.scrud.copy

import org.scalatest.{MustMatchers, FunSpec}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.github.scrud.context.RequestContext

/**
 * A behavior specification for [[com.github.scrud.copy.CompositeSourceField]].
 * @author Eric Pabst (epabst@gmail.com)
 *         Date: 3/18/14
 *         Time: 9:32 AM
*/
@RunWith(classOf[JUnitRunner])
class CompositeSourceFieldSpec extends FunSpec with MustMatchers {
  describe("findValue") {
    it("must have a very shallow stack when delegating to an SourceField's findValue") {
      val sourceField = new SourceField[String] {
        /** Get some value or None from the given source. */
        override def findValue(source: AnyRef, context: RequestContext) = {
          val throwable = new Throwable()
          throwable.fillInStackTrace()
          throwable.printStackTrace()
          throwable.getStackTrace.indexWhere(_.getMethodName == "findValue", 2) must be <= 4
          Some("the result")
        }
      }
      val composite = new CompositeSourceField[String](Seq(sourceField))
      composite.findValue(new Object, null) must be (Some("the result"))
    }
  }
}
