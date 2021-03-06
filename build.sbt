organization := "org.scala_users.jp"

name := "finagle_echo"

version := "0.0.0.0.1"

scalaVersion := "2.9.1"

resolvers += "twitter-repo" at "http://maven.twttr.com"

resolvers += "kmizu-repo" at "http://kmizu.github.com/maven/"

libraryDependencies ++= Seq(
  "com.twitter" % "finagle-core_2.9.1" % "2.0.0",
  "com.twitter" % "finagle-http_2.9.1" % "2.0.0",
  "com.twitter" % "finagle-stream_2.9.1" % "2.0.0"
)

libraryDependencies ++= Seq(
  "net.liftweb" %% "lift-json" % "2.4",
  "org.onion_lang" %% "jsonic" % "0.0.1"
)

EclipseKeys.withSource := true

resolvers ++= Seq("snapshots" at "http://oss.sonatype.org/content/repositories/snapshots", "releases"  at "http://oss.sonatype.org/content/repositories/releases")

publishTo := Some(Resolver.file("Github Pages", Path.userHome / "git" / "kmizu.github.com" / "maven" asFile)(Patterns(true, Resolver.mavenStyleBasePattern)))

publishMavenStyle := true

scalacOptions ++= Seq("-deprecation","-unchecked")

initialCommands in console += {
  Iterator("net.liftweb.json._").map("import "+).mkString("\n")
}
