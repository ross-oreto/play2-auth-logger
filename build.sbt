name := """play2-auth-logger"""
version := "1.0-SNAPSHOT"
organization := "org.oreto"
scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.9",
  "org.slf4j" % "slf4j-simple" % "1.7.9",
  "com.typesafe.play" %% "play" % "2.5.7",
  "com.typesafe.play" %% "play-cache" % "2.5.7",
  "jp.t2v" %% "play2-auth" % "0.14.2",
  "jp.t2v" %% "play2-auth-test"   % "0.14.2" % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

publishTo := {
  val prefix = if (isSnapshot.value) "snapshots" else "releases"
  Some(s3resolver.value(prefix+" S3 bucket", s3("org.oreto.maven/" + prefix)))
}