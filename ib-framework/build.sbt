name := "ib-framework"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += "Mesosphere Repo" at "http://downloads.mesosphere.io/maven"

libraryDependencies += "org.apache.mesos" % "mesos" % "0.28.1"

libraryDependencies += "org.mongodb" %% "casbah" % "3.1.1"

libraryDependencies += "org.json4s" % "json4s-native_2.11" % "3.3.0"

libraryDependencies += "com.datastax.cassandra" % "cassandra-driver-core" % "3.0.2"

scalacOptions += "-Ylog-classpath"

unmanagedJars in Compile += file("lib/t-streams.jar")