package com.ib.framework

import scala.util.Properties._

/**
  * Created by diryavkin_dn on 01.07.16.
  */
object Config {
  var memPerTask: Double = 128
  var cpuPerTask: Double = 0.1
  var diskPerTask: Double = 64

  val mongoHost = envOrElse("MONGO_HOST", "127.0.0.1")
  val mongoPort = envOrElse("MONGO_PORT", "27017").toInt
  val mongoDB = envOrElse("MONGO_DB", "testdb")
  val mongoCollection = envOrElse("MONGO_COLLECTION", "stocks")

  val zkHost = envOrElse("ZK_HOST", "127.0.0.1")
  val zkPort = envOrElse("ZK_PORT", "2181").toInt

  val cassandraHost = envOrElse("CASSANDRA_HOST", "127.0.0.1")

  val rootPath = envOrElse("ROOT_PATH", "/unit")
  val benchmarkPath = "/benchmark"
  var keyspace = "keyspace"
  val agent_address = "176.120.25.19"
  val urlParams = scala.util.Properties.envOrElse("PARAMS_URL", "http://127.0.0.1/params.json")
  val jarUri = envOrElse("JAR_URI", "http://127.0.0.1/jar.jar")
  val jarName = envOrElse("JAR_NAME", "jar.jar")
  val command = "java -jar " + jarName


  def newKeyspace(cnt:Int) = {
    keyspace = "keyspace_" + cnt + "_" + RandomStringCreator.randomAlphaString(10)
  }
}
