package com.ib.framework

import scala.util.Properties._

/**
  * Created by diryavkin_dn on 01.07.16.
  */
object Config {
  var memPerTask: Double = 128
  var cpuPerTask: Double = 0.1
  var diskPerTask: Double = 64
  val zkHost = envOrElse("ZK_HOST", "176.120.25.19")
  val zkPort = envOrElse("ZK_PORT", "2181").toInt
  val rootPath = envOrElse("ROOT_PATH", "/unit")
  val benchmarkPath = "/benchmark"
  var keyspace = "keyspace"
  val agent_address = "176.120.25.19"
  val command = "java -jar ib-agent.jar"
  def newKeyspace(cnt:Int) = {
    keyspace = "keyspace_" + cnt + "_" + RandomStringCreator.randomAlphaString(10)
  }
}
