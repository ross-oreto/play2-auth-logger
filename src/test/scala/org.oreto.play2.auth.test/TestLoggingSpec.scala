package org.oreto.play2.auth.test

import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.mvc.Result
import play.api.test.Helpers._

import scala.concurrent.Future

class TestLoggingSpec extends PlaySpec with OneAppPerSuite  {

  "test logging" should {
    "should be ok" in {
      val controller = new TestLoggingController()
      val result: Future[Result] = controller.test().apply(controller.testRequest)
      val bodyText: String = contentAsString(result)
      bodyText mustBe "ok"
    }
  }

  "test auth logging" should {
    "should be ok" in {
      val controller = new TestAuthLoggingController()
      val result: Future[Result] = controller.test().apply(controller.testRequest)
      val bodyText: String = contentAsString(result)
      bodyText mustBe "ok"
    }
  }
}
