package outwatch
package router

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PathTestSpec extends AnyFlatSpec with Matchers {
  class PathTest(url: String, path: Path) {
    path.toUrlString should equal(url)
  }

  "Root url" should "be /" in new PathTest("/", Root)
  "A 1 part path" should "be correct" in new PathTest("/search", Root / "search")
}
