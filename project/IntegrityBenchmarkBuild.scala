import sbt.Keys._
import sbt._

object IntegrityBenchmarkBuild extends Build {

  addCommandAlias("rebuild", ";clean; compile; package")

  //////////////////////////////////////////////////////////////////////////////
  // PROJECTS
  //////////////////////////////////////////////////////////////////////////////
  lazy val ib = Project(id = "ib",
    base = file("."),
    settings = commonSettings) aggregate(agent, framework)


    lazy val agent = Project(id = "ib-agent",
      base = file("ib-agent"))

    lazy val framework = Project(id = "ib-framework",
      base = file("ib-framework"))



  //////////////////////////////////////////////////////////////////////////////
  // PROJECT INFO
  //////////////////////////////////////////////////////////////////////////////

  val ORGANIZATION = "com.bwsw"
  val PROJECT_NAME = "Integrity Benchmark"
  val PROJECT_VERSION = "0.1-SNAPSHOT"
  val SCALA_VERSION = "2.11.8"


  //////////////////////////////////////////////////////////////////////////////
  // DEPENDENCY VERSIONS
  //////////////////////////////////////////////////////////////////////////////

  val TYPESAFE_CONFIG_VERSION = "1.3.0"
  val SCALATEST_VERSION = "2.2.4"


  //////////////////////////////////////////////////////////////////////////////
  // SHARED SETTINGS
  //////////////////////////////////////////////////////////////////////////////

  lazy val commonSettings = Project.defaultSettings ++
    basicSettings


  lazy val basicSettings = Seq(
    version := PROJECT_VERSION,
    organization := ORGANIZATION,
    scalaVersion := SCALA_VERSION,
    fork in run := true,
    fork in Test := true,
    parallelExecution in Test := false
  )
}
