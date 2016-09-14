### Play 2 Auth Logger

This is a trait which adds access logging for actions and play2-auth stack actions.

* Installation
```
resolvers += "Releases resolver" at "https://s3-us-west-2.amazonaws.com/org.oreto.maven/releases"
resolvers += "Snapshots resolver" at "https://s3-us-west-2.amazonaws.com/org.oreto.maven/snapshots"
"org.oreto" % "play2-auth-logger_2.11" % "1.0-SNAPSHOT"
```

* Usage for normal actions
```
class Application extends Controller with LoggingController {

  def action1 = LoggingAction { implicit request =>
    Ok("Ok")
  }

  def action2 = LoggingAction.async { implicit request =>
    Future {
        Ok("Ok")
    }
  }
```

* Usage to integrate with play2-auth
```
class HomeController extends Controller with AuthConfigImpl with AuthLogger {

  def action1 = AuthLogging(AuthorityKey -> APP_USER) {
    Action { implicit request =>
       Ok(loggedInUser.get.toString)
    }
  }
  
  def action2 = AuthLogging() {
    Action.async { implicit request =>
      Future {
        Ok(loggedInUser.get.toString)
      }
    }
  }

}
```

If you are using the AuthenticationElement or OptionalAuthElement instead of AuthElement, the corresponding traits are available
```
with AuthenticationLogger
with OptionalAuthLogger
```

When using the AuthLogger the logged in user is stored and the variable 'loggedInUser' is available in the implementing controller
```
implicit var loggedInUser: Option[User] = None
```

By default log entries are written to a Logger with name "application"
This can be changed using override:
```
override val loggerName = "access"
```

Using these actions automatically writes log entries to the specified access log in the form:
```
2016-09-10 22:34:32 -0500 0:0:0:0:0:0:0:1 - localhost:9000 - GET: http /login body-size: None, ref: http://localhost:9000/report, agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.89 Safari/537.36, time: 0, response-size: 1710, status: 200
2016-09-10 22:34:42 -0500 user: oretomr, 0:0:0:0:0:0:0:1 - localhost:9000 - GET: http /report body-size: None, ref: http://localhost:9000/login, agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.89 Safari/537.36, time: 0, response-size: 25199, status: 200
```

To log the 303 redirects which take place for authenticated actions use the following AuthConfig override in the route Controller
```
  override def authenticationFailed(request: RequestHeader)(implicit context: ExecutionContext): Future[Result] = {
    val startTime: Long = now
    super.authenticationFailed(request).map(response => {
      writeToAccessLog(request, Some(response), startTime)
      response
    })
  }
```

Now the auth fail redirects will show up as 303 redirect access log entries
```
2016-09-10 22:34:31 -0500 0:0:0:0:0:0:0:1 - localhost:9000 - GET: http /report body-size: None, ref: http://localhost:9000/report, agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.89 Safari/537.36, time: 0, response-size: 0, status: 303
2016-09-10 22:34:32 -0500 0:0:0:0:0:0:0:1 - localhost:9000 - GET: http /login body-size: None, ref: http://localhost:9000/report, agent: Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.89 Safari/537.36, time: 0, response-size: 1710, status: 200
```

To change how the log entries are written override the following function
```
def writeToAccessLog(request: RequestHeader, result: Option[Result] = None, startTime: Long = now, user: Option[String] = None): Unit
```


####Note: 
This will not log entries for assets and or webjars. 
If those are needed you could create a filter which logs those requests specifically looking for /assets or /webjars uri patterns.