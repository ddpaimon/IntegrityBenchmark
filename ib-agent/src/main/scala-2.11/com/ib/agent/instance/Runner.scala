package com.ib.agent.instance

import com.bwsw.tstreams.agents.consumer.BasicConsumerTransaction
import com.bwsw.tstreams.agents.producer.ProducerPolicies
import com.bwsw.tstreams.common.zkservice.ZkService
//import com.bwsw.tstreamstest.Main._
import org.apache.log4j.Logger
import org.apache.zookeeper.{WatchedEvent, Watcher}

import scala.collection.mutable
import scala.util.Properties._

/**
  * Created by diryavkin_dn on 27.06.16.
  */
object Runner {
  val logger = Logger.getLogger(getClass)
  def randomString: String = RandomStringCreator.randomAlphaString(10)
  val zkService = new ZkService(Config.rootPath+Config.benchmarkPath, List(new java.net.InetSocketAddress("192.168.1.225", 2181)), 60)




  def runProducer() = {
    logger.info("prod="+Config.Keyspace)
    val r = scala.util.Random
    val totalDataInTxn = Config.transactionLength
    val sendData = (for (part <- 0 until totalDataInTxn) yield "data_part_" + part).sorted
    val pr1 = Instance.getProducer
    val pr2 = Instance.getProducer
    val pr3 = Instance.getProducer
    val pr4 = Instance.getProducer
    val instance = Instance.getProducer
    var transactionsCount = 0
    for (transactionIndex <- 1 to Config.transactionCount) {
      val txn = instance.newTransaction(ProducerPolicies.errorIfOpen)
      if (r.nextFloat > Config.rollback) {
        transactionsCount += 1
        logger.info(s"Sent: ${txn.getTxnUUID}")
        sendData.foreach { x => txn.send(x); Thread.sleep(50)}
        txn.checkpoint()
      } else {
        txn.cancel
        logger.info(s"Canceled: ${txn.getTxnUUID}")
      }
      logger.info("PROD: "+pr1)
      logger.info("PROD: "+pr2)
      logger.info("PROD: "+pr3)
      logger.info("PROD: "+pr4)
      println(pr1)
      println(pr2)
      println(pr3)
      println(pr4)
    }
    val instancePath = "/"+Config.Keyspace+"/"+instance.name.toString
    if (zkService.exist(instancePath)) zkService.setData(instancePath, "{\"T-count\":" + "\""+transactionsCount+"\"}")
    else zkService.create(instancePath, transactionsCount, org.apache.zookeeper.CreateMode.PERSISTENT)
//    System.exit(0)
  }




  def runConsumer():Unit = {
    val receivedTransactions:mutable.ListBuffer[com.bwsw.tstreams.agents.consumer.BasicConsumerTransaction[_,_]] = mutable.ListBuffer()
    val instance = Instance.getConsumer
    val watcher = new Watcher {
      override def process(event: WatchedEvent): Unit = {
        logger.info("Run consumer watcher process" + "PATH: " + "/"+Config.Keyspace)
        val data = zkService.get[Int]("/"+Config.Keyspace)
        val instancePath = "/"+Config.Keyspace+"/"+instance.name.toString
        if (zkService.exist(instancePath)) zkService.setData(instancePath,receivedTransactions.length)
        else zkService.create(instancePath, receivedTransactions.length, org.apache.zookeeper.CreateMode.PERSISTENT)
        System.exit(0)
      }
    }
    zkService.setWatcher("/"+Config.Keyspace, watcher)
    while (true) {
      val consumedTxn: Option[BasicConsumerTransaction[Array[Byte], String]] = instance.getTransaction
      if (consumedTxn.isDefined) {
          receivedTransactions += consumedTxn.get
        logger.info(s"TRANSACTION: $consumedTxn")
      } else {
        logger.info("No transaction")
      }
      Thread.sleep((Config.consumerCheckInterval * 1000).toInt)
    }
  }


  def runMaster() = {
    val r = scala.util.Random
    val instance = Instance.getMaster
    var check = true
    println(s"Master: $instance")
    def oncePerSecond(callback: () => Unit) {
      while (check) {
        callback()
        Thread.sleep(1000)
      }
    }
    def checkCompletion() = {
      if (r.nextFloat < Config.masterProbability) {
        if (r.nextFloat > Config.masterCrash) {
          println("Stop")
          instance.stop()
          check = false
        } else {
          println("Crash")
          System.exit(1)
        }
      } else {
        println("Alive")
      }
    }
    oncePerSecond(() => checkCompletion())
  }
}
