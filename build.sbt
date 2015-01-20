name := """play-api-example"""

version := "1.0"

lazy val root = (project in file("."))
  .enablePlugins(play.PlayScala)
  .aggregate(spracebook)
  .dependsOn(spracebook)
lazy val spracebook = uri("git://github.com/jdauphant/spracebook.git")

scalaVersion := "2.11.4"

resolvers ++= Seq(
  "Spray" at "http://repo.spray.io/"
)

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo"  % "0.10.5.0.akka23",
  "org.mindrot"       %  "jbcrypt"              % "0.3m",
  "io.spray"          %% "spray-client"         % "1.3.2",
  "io.spray"          %% "spray-json"           % "1.3.1"
)

scalacOptions ++= Seq("-unchecked", "-deprecation","-feature")