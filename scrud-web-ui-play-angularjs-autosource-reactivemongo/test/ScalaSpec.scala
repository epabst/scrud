import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

// Should provide some tests once
class ScalaSpec extends FlatSpec with ShouldMatchers {
  "1 + 1" should "equal 2" in {
    1 + 1 should equal (2)
  }
}
