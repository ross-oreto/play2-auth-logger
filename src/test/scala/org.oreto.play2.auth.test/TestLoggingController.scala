package org.oreto.play2.auth.test

import jp.t2v.lab.play2.auth.AuthConfig
import org.oreto.play2.auth.{AuthenticationLogger, LoggingController}
import play.api.mvc.{Action, Controller, RequestHeader, Result}
import play.api.test.FakeRequest

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.{ClassTag, classTag}


class TestLoggingController extends Controller with LoggingController {

  lazy val testRequest = FakeRequest().withHeaders("Host" -> "FAKE", "User-Agent" -> "test")

  def test() = LoggingAction { implicit request =>
    Ok("ok")
  }
}

class TestAuthLoggingController extends Controller with AuthenticationLogger with TestAuthConfigImpl {

  import jp.t2v.lab.play2.auth.test.Helpers._

  lazy val testRequest = FakeRequest().withHeaders("Host" -> "FAKE", "User-Agent" -> "test").withLoggedIn(config)(testUser)

  def test() = AuthLogging() {
    Action { implicit request =>
        Ok("ok")
    }
  }
}

trait TestAuthConfigImpl extends AuthConfig {
  import play.api.mvc.Results.{Forbidden, Ok}
  val testUser = "testuser"
  type Id = String
  type User = String
  type Authority = String

  override val idTag: ClassTag[Id] = classTag[Id]
  override def sessionTimeoutInSeconds: Int = 3600
  override def resolveUser(id: String)(implicit context: ExecutionContext): Future[Option[String]] = Future.successful{ Some(testUser) }
  override def loginSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = Future { Ok("ok") }
  override def logoutSucceeded(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = Future { Ok("ok") }
  override def authenticationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = Future { Ok("ok") }
  override def authorizationFailed(request: RequestHeader, user: String, authority: Option[String])(implicit context: ExecutionContext): Future[Result] =
    Future { Forbidden("not authorized") }
  override def authorize(user: String, authority: String)(implicit context: ExecutionContext): Future[Boolean] = Future { true }
}
object config extends TestAuthConfigImpl
