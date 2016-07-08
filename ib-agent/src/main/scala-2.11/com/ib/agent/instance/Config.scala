package com.ib.agent.instance

import scala.util.Properties._


/**
  * Created by diryavkin_dn on 30.06.16.
  */
object Config {
  def randomString: String = RandomStringCreator.randomAlphaString(10)
  val Keyspace = envOrElse("CASSANDRA_KEYSPACE", randomString)
  val cassandraHost = envOrElse("CASSANDRA_HOST", "192.168.1.225")
  val cassandraPort = envOrElse("CASSANDRA_PORT", "9042").toInt
  val zookeeperHost = envOrElse("ZK_HOST", "172.120.25.19")
  val zookeeperPort = envOrElse("ZK_PORT", "2181").toInt
  val address = envOrElse("AGENT_ADDRESS", "192.168.1.225:8888")
  val taskId = envOrElse("TASK_ID", "TaskId")
  val rootPath = envOrElse("ROOT_PATH", "/unit")
  val benchmarkPath = envOrElse("BENCHMARK_PATH", "/benchmark")


  //producer settings
  val transportTimeout = envOrElse("TRANSPORT_TIMEOUT", "5").toInt
  val TTL = envOrElse("TTL", "30").toInt
  val transactionKeepAliveInterval = envOrElse("TRANSACTION_KAI", "2").toInt
  val producerKeepAliveInterval = envOrElse("PRODUCER_KAI", "1").toInt

  //consumer settings
  val transactionPreload = envOrElse("TRANSACTION_PRELOAD", "10").toInt
  val dataPreload = envOrElse("DATA_PRELOAD", "7").toInt
  val consumerKeepAliveInterval = envOrElse("CONSUMER_KAI", "5").toInt
  val useLastOffset = envOrElse("USE_LATS_OFFSET", "false").toBoolean

  val keyspace = envOrElse("CASSANDRA_KEYSPACE", randomString)
  val agentType = envOrElse("TYPE", "0").toInt
  val transactionLength = envOrElse("TRANSACTION_LENGTH", "10").toInt
  val transactionCount = envOrElse("TRANSACTION_COUNT", "10").toInt
  val rollback = envOrElse("ROLLBACK", "0.5").toDouble
  val consumerCheckInterval = envOrElse("CONSUMER_CHECK_INTERVAL", "1").toDouble
  val consumerCheckCount = envOrElse("CONSUMER_CHECK_COUNT", "10").toInt
  val masterProbability = envOrElse("MASTER_PROBABILITY", "0.5").toDouble
  val masterCrash = envOrElse("MASTER_CRASH", "0.5").toDouble
  val consumerTransactionCount = envOrElse("CONSUMER_TRANSACTION_COUNT", "10").toInt
}
