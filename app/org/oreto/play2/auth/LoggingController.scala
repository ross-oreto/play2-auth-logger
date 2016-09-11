package org.oreto.play2.auth

import jp.t2v.lab.play2.auth.{AuthConfig, AuthElement, AuthenticationElement, OptionalAuthElement}
import jp.t2v.lab.play2.stackc.{Attribute, RequestWithAttributes, StackableController}
import play.api.Logger
import play.api.mvc.{Controller, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait LoggingController {
  self: Controller =>

  val accessLoggerName = "application"

  lazy val now = System.currentTimeMillis
  lazy val accessLogger: Logger = Logger(accessLoggerName)

  object LoggingAction extends ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
      block(request)
    }
    override def composeAction[A](action: Action[A]) = Logging(action)
  }

  def Logging[A](action: Action[A], user: Option[String] = None) = Action.async(action.parser) { request =>
    val startTime: Long = now
    action(request).map ( response => {
      writeToAccessLog(request, Some(response), startTime, user)
      response
    })
  }

  def writeToAccessLog(request: RequestHeader, result: Option[Result] = None, startTime: Long = now, user: Option[String] = None): Unit = {
    val userAgent = request.headers.get("User-Agent")
    if(userAgent.isDefined && !userAgent.get.contains("nagios")) {
      val endTime = now
      val requestTime = endTime - startTime
      val protocol = if (request.secure) "https" else "http"
      val remoteAddress = request.headers.get("X-Forwarded-For").getOrElse(request.remoteAddress)
      val referer = request.headers.get("Referer").getOrElse("None")
      val contentLength = request.headers.get("Content-Length").getOrElse("None")

      var line = s"$remoteAddress - ${request.host} - ${request.method}: $protocol ${request.uri}" +
        s" body-size: $contentLength, ref: $referer, agent: ${userAgent.get}, time: $requestTime"

      if (result.isDefined) {
        val resultSize = result.get.body.contentLength.getOrElse(0)
        line = line + s", response-size: $resultSize, status: ${result.get.header.status}"
      }

      accessLogger.info(if (user.isDefined) s"user: ${user.get}, $line" else line)
    }
  }
}

trait BaseAuthLogger extends LoggingController with AuthConfig with StackableController{
  self: Controller =>

  implicit var loggedInUser: Option[User] = None

  def AuthLogging[A](params: Attribute[_]*)(action: Action[A]) = AsyncStack(action.parser, params: _*) { implicit requestWithAttributes =>
    setLoggedInUser(requestWithAttributes)
    val startTime: Long = now
    action(requestWithAttributes).map ( response => {
      writeToAccessLog(requestWithAttributes, Some(response), startTime, Some(loggedInUser.getOrElse().toString))
      response
    })
  }

  def setLoggedInUser(request: RequestWithAttributes[_])
}

trait AuthLogger extends BaseAuthLogger with AuthElement {
  self: Controller =>
  override def setLoggedInUser(request: RequestWithAttributes[_]) { this.loggedInUser = Some(loggedIn(request)) }
}

trait AuthenticationLogger extends BaseAuthLogger with AuthenticationElement {
  self: Controller =>
  override def setLoggedInUser(request: RequestWithAttributes[_]) { this.loggedInUser = Some(loggedIn(request)) }
}

trait OptionalAuthLogger extends BaseAuthLogger with OptionalAuthElement {
  self: Controller =>

  override def setLoggedInUser(request: RequestWithAttributes[_]) { this.loggedInUser = loggedIn(request) }
}
