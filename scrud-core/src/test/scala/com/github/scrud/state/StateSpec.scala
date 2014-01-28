package com.github.scrud.state

//import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.FunSpec
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

/** A behavior specification for [[com.github.scrud.state.State]].
  * @author Eric Pabst (epabst@gmail.com)
  */

//@RunWith(classOf[JUnitRunner])
class StateSpec extends FunSpec with MustMatchers with MockitoSugar {
  it("must retain its value for the same State") {
    object myVar extends StateVar[String]
    val state = new State
    val state2 = new State
    myVar.get(state) must be (None)
    myVar.set(state, "hello")
    myVar.get(state) must be (Some("hello"))
    myVar.get(state2) must be (None)
    myVar.get(state) must be (Some("hello"))
  }

  it("clear must clear the value for the same State") {
    object myVar extends StateVar[String]
    val myVar2 = new StateVar[String]
    val state = new State
    val state2 = new State
    myVar.set(state, "hello")
    myVar2.set(state, "howdy")

    myVar.clear(state2) must be (None)
    myVar.get(state) must be (Some("hello"))

    myVar.clear(state) must be (Some("hello"))
    myVar.get(state) must be (None)

    myVar2.clear(state) must be (Some("howdy"))
  }

  describe("getOrSet") {
    object StringVar extends StateVar[String]
    trait Computation {
      def evaluate: String
    }

    it("must evaluate and set if not set yet") {
      val computation = mock[Computation]
      when(computation.evaluate).thenReturn("result")
      val vars = new State
      StringVar.getOrSet(vars, computation.evaluate) must be ("result")
      verify(computation).evaluate
    }

    it("must evaluate only the first time") {
      val computation = mock[Computation]
      when(computation.evaluate).thenReturn("result")
      val vars = new State
      StringVar.getOrSet(vars, computation.evaluate)
      StringVar.getOrSet(vars, computation.evaluate) must be ("result")
      verify(computation, times(1)).evaluate
    }

    it("must not evaluate if already set") {
      val vars = new State
      StringVar.set(vars, "hello")
      StringVar.getOrSet(vars, throw new IllegalArgumentException("shouldn't happen")) must be ("hello")
    }
  }
}
