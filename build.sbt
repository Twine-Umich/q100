
val commonSettings = Seq(
  scalaVersion := "2.12.12",
  crossScalaVersions := Seq("2.12.12", "2.11.12"),
  libraryDependencies ++= Seq(
    "edu.umich.engin.eecs" %% "chisel3" % "0.1-SNAPSHOT",
    "org.scalatest" %% "scalatest" % "3.0.1"
  ),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases")
  )
)

val miniSettings = commonSettings ++ Seq(
  name := "q100",
  version := "1.0",
  organization := "edu.umich.engin.eecs")

lazy val lib  = project settings commonSettings
lazy val mini = project in file(".") settings miniSettings dependsOn lib
