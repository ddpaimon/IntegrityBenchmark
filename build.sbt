name := "IntegrityBenchmark"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "log4j" % "log4j" % "1.2.17"
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.21"

unmanagedJars in Compile += file("lib/t-streams.jar")