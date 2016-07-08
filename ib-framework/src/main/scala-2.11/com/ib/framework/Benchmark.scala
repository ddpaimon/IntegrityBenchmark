package com.ib.framework

/**
  * Created by diryavkin_dn on 02.06.16.
  */

import org.apache.mesos.Protos.{CommandInfo, Environment}
import org.json4s._

import collection.mutable
import scala.collection.JavaConverters._


class Benchmark() {
  var consumerParams:Iterable[_<:Environment.Variable] = Iterable()
  var producerParams:Iterable[_<:Environment.Variable] = Iterable()
  var masterParams:Iterable[_<:Environment.Variable] = Iterable()
  var commonParams:Iterable[_<:Environment.Variable] = Iterable()


  var name:String = null
  var count:Int = this.count
  var producer:Int = this.producer
  var consumer:Int = this.consumer
  var master:Int = this.master
  var transactionLength:String = this.transactionLength
  var transactionCount:String = this.transactionCount
  var rollback:Float = this.rollback
  var masterCrash:String = this.masterCrash
  var masterCompletion:String = this.masterCompletion
  var masterRestart:List[Any] = this.masterRestart

  var TTL:String = this.TTL
  var transactionTimeout:String = this.transactionTimeout
  var transactionKeepAliveInterval:String = this.transactionKeepAliveInterval
  var producerKeepAliveInterval:String = this.producerKeepAliveInterval
  var rootPath:String = this.rootPath

  var transactionPreload:String = this.transactionPreload
  var dataPreload:String = this.dataPreload
  var consumerKeepAliveInterval:String = this.consumerKeepAliveInterval
  var useLastOffset:String = this.useLastOffset
  var consumerCheckInterval:String = this.consumerCheckInterval
  var consumerCheckCount:String = this.consumerCheckCount
  var consumerTransactionCount:String = this.consumerTransactionCount


  def this(params:JValue) {
    this
    name = (params\"name").values.toString
    count = (params\"count").values.toString.toInt
    producer = (params\\"producer").values.toString.toInt
    master = (params\\"master").values.toString.toInt
    consumer = (params\\"consumer").values.toString.toInt
    transactionLength = (params\\"transactionLength").values.toString
    transactionCount = (params\\"transactionCount").values.toString
    rollback = (params\\"rollback").values.toString.toFloat
    masterCrash = (params\\"masterCrash").values.toString
    masterCompletion = (params\\"masterCompletion").values.toString
    masterRestart = (params\\"masterRestart").productIterator.toList

    TTL = (params\\"transactionTTL").values.toString
    rootPath = (params\\"rootPath").values.toString
    transactionTimeout = (params\\"transactionTimeout").values.toString
    transactionKeepAliveInterval = (params\\"transactionKeepAliveInterval").values.toString
    producerKeepAliveInterval = (params\\"producerKeepAliveInterval").values.toString

    transactionPreload = (params\\"transactionPreload").values.toString
    dataPreload = (params\\"dataPreload").values.toString
    consumerKeepAliveInterval = (params\\"consumerKeepAliveInterval").values.toString
    useLastOffset = (params\\"useLastOffset").values.toString
    consumerCheckInterval = (params\\"consumerCheckInterval").values.toString
    consumerCheckCount = (params\\"consumerCheckCount").values.toString
    consumerTransactionCount = (params\\"consumerTransactionCount").values.toString

  }



  def validate(): Boolean = {
    true
  }
  def prepareCommands(urlJar:String): mutable.Map[String, CommandInfo.Builder] = {
//

    val commands: mutable.Map[String, CommandInfo.Builder] = mutable.Map()
    val command = "java -jar t-streams-test.jar"

    val TaskId = Environment.Variable.newBuilder.setName("TASK_ID")
    val ttype = Environment.Variable.newBuilder.setName("TYPE")
//

    //common settings
    {
      val casKeyspace = Environment.Variable.newBuilder.setName("CASSANDRA_KEYSPACE").setValue(Config.keyspace).build
      val casHost = Environment.Variable.newBuilder.setName("CASSANDRA_HOST").setValue("192.168.1.225").build
      val casPort = Environment.Variable.newBuilder.setName("CASSANDRA_PORT").setValue("9042").build
      val zkHost = Environment.Variable.newBuilder.setName("ZK_HOST").setValue(Config.zkHost).build
      val zkPort = Environment.Variable.newBuilder.setName("ZK_PORT").setValue(Config.zkPort.toString).build
      val rootPath = Environment.Variable.newBuilder.setName("ROOT_PATH").setValue(Config.rootPath).build
      val benchmarkPath = Environment.Variable.newBuilder.setName("BENCHMARK_PATH").setValue(Config.benchmarkPath).build

      commonParams = Iterable(
        casKeyspace,
        casHost,
        casPort,
        zkHost,
        zkPort,
        rootPath,
        benchmarkPath)
    }


    //master settings
    {
      val masterCompletion = Environment.Variable.newBuilder.setName("MASTER_PROBABILITY").setValue(this.masterCompletion).build
      val masterCrash = Environment.Variable.newBuilder.setName("MASTER_CRASH").setValue(this.masterCrash).build

      masterParams = Iterable(
        masterCompletion,
        masterCrash)
    }


    //producer settings
    {
      val transportTimeout = Environment.Variable.newBuilder.setName("TRANSPORT_TIMEOUT").setValue(this.transactionTimeout).build
      val TTL = Environment.Variable.newBuilder.setName("TTL").setValue(this.TTL).build
      val transactionKeepAliveInterval = Environment.Variable.newBuilder.setName("TRANSACTION_KAI").setValue(this.transactionKeepAliveInterval).build
      val producerKeepAliveInterval = Environment.Variable.newBuilder.setName("PRODUCER_KAI").setValue(this.producerKeepAliveInterval).build
      val transactionCount = Environment.Variable.newBuilder.setName("TRANSACTION_COUNT").setValue(this.transactionCount).build
      val transactionLength = Environment.Variable.newBuilder.setName("TRANSACTION_LENGTH").setValue(this.transactionLength).build

      producerParams = Iterable(
        transportTimeout,
        TTL,
        transactionKeepAliveInterval,
        producerKeepAliveInterval,
        transactionCount,
        transactionLength)
    }


    //consumer settings
    {
      val transactionPreload = Environment.Variable.newBuilder.setName("TRANSACTION_PRELOAD").setValue(this.transactionPreload).build
      val dataPreload = Environment.Variable.newBuilder.setName("DATA_PRELOAD").setValue(this.dataPreload).build
      val consumerKeepAliveInterval = Environment.Variable.newBuilder.setName("CONSUMER_KAI").setValue(this.consumerKeepAliveInterval).build
      val useLastOffset = Environment.Variable.newBuilder.setName("USE_LATS_OFFSET").setValue(this.useLastOffset).build
      val consumerCheckInterval = Environment.Variable.newBuilder.setName("CONSUMER_CHECK_INTERVAL").setValue(this.consumerCheckInterval).build
      val consumerCheckCount = Environment.Variable.newBuilder.setName("CONSUMER_CHECK_COUNT").setValue(this.consumerCheckCount).build
      val consumerTransactionCount = Environment.Variable.newBuilder.setName("CONSUMER_TRANSACTION_COUNT").setValue(this.consumerTransactionCount).build

      consumerParams = Iterable(
        transactionPreload,
        dataPreload,
        consumerKeepAliveInterval,
        useLastOffset,
        consumerCheckInterval,
        consumerCheckCount,
        consumerTransactionCount)
    }
    val ports = collection.mutable.Queue[Int]()
    (31000 to 32000).foreach(ports+=_)



    for (i <- 1 to producer) {
      val port = ports.dequeue()
      val taskId:String = 0+"_"+java.util.UUID.randomUUID.toString+"_producer"
      val env = Environment.newBuilder
        .addAllVariables(commonParams.asJava)
        .addAllVariables(producerParams.asJava)
        .addVariables(TaskId.setValue(taskId))
        .addVariables(ttype.setValue("0"))
        .addVariables(Environment.Variable.newBuilder.setName("AGENT_ADDRESS").setValue(Config.agent_address+":"+port))
      val cmd = CommandInfo.newBuilder
        .addUris(CommandInfo.URI.newBuilder.setValue(urlJar))
          .setValue(command)
        .setEnvironment(env.build)
      commands += taskId -> cmd
    }

    for (i <- 1 to consumer) {
      val port = ports.dequeue()
      val taskId: String = 2+"_"+java.util.UUID.randomUUID.toString + "_consumer"
      val env = Environment.newBuilder
        .addAllVariables(commonParams.asJava)
        .addAllVariables(consumerParams.asJava)
        .addVariables(ttype.setValue("2"))
        .addVariables(TaskId.setValue(taskId))
        .addVariables(Environment.Variable.newBuilder.setName("AGENT_ADDRESS").setValue(Config.agent_address+":"+port))
      val cmd = CommandInfo.newBuilder
        .addUris(CommandInfo.URI.newBuilder.setValue(urlJar))
        .setValue(command)
        .setEnvironment(env.build)
      commands += taskId -> cmd
    }

    for (i <- 1 to master) {
      val port = ports.dequeue()
      val taskId:String = 1+"_"+java.util.UUID.randomUUID.toString+"_master"
      val env = Environment.newBuilder
        .addAllVariables(commonParams.asJava)
        .addAllVariables(masterParams.asJava)
        .addVariables(ttype.setValue("1"))
        .addVariables(TaskId.setValue(taskId))
        .addVariables(Environment.Variable.newBuilder.setName("AGENT_ADDRESS").setValue(Config.agent_address+":"+port))
      val cmd = CommandInfo.newBuilder
        .addUris(CommandInfo.URI.newBuilder.setValue(urlJar))
        .setValue(command)
        .setEnvironment(env.build)
      commands += taskId -> cmd
    }
      commands
    }


}
