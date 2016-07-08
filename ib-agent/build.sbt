name := "ib-agent"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "log4j" % "log4j" % "1.2.17"





//COORDINATION
//resolvers += "twitter resolver" at "http://maven.twttr.com"
//libraryDependencies += ("com.twitter.common.zookeeper" % "lock" % "0.0.38")
////  .exclude("com.google.guava","guava")
//  .exclude("org.slf4f", "slf4j-api")
//  .exclude("log4j","log4j")
////  .exclude("io.netty", "netty")
//  .exclude("org.slf4j","slf4j-log4j12")
//  .exclude("org.apache.zookeeper", "zookeeper")

//libraryDependencies += ("org.apache.zookeeper" % "zookeeper" % "3.4.6")
//  .exclude("org.slf4j","slf4j-log4j12")


scalacOptions += "-Ylog-classpath"
assemblyJarName in assembly := "ib-agent.jar"

assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "log4j", xs@_*) => MergeStrategy.first
  case "application.conf" => MergeStrategy.concat
  case "library.properties" => MergeStrategy.concat
  case "log4j.properties" => MergeStrategy.concat
//  case "unwanted.txt" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

unmanagedJars in Compile += file("lib/t-streams.jar")