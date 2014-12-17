name := """play-api-example"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(play.PlayScala)

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  cache
)

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
  "org.mindrot" % "jbcrypt" % "0.3m"
)

PlayKeys.playWatchService := play.runsupport.PlayWatchService.sbt(pollInterval.value)
