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
    val instance = Instance.getProducer
    var transactionsCount = 0
    logger.info("Start send message")
    for (transactionIndex <- 1 to Config.transactionCount) {
      logger.info(transactionIndex)
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
    }
    val instancePath = "/"+Config.Keyspace+"/producers/"+instance.name.toString
    if (zkService.exist(instancePath)) zkService.setData(instancePath, transactionsCount)
    else zkService.create(instancePath, transactionsCount, org.apache.zookeeper.CreateMode.PERSISTENT)
    System.exit(0)
  }



  def runConsumer():Unit = {
    val receivedTransactions:mutable.ListBuffer[com.bwsw.tstreams.agents.consumer.BasicConsumerTransaction[_,_]] = mutable.ListBuffer()
    val instance = Instance.getConsumer

    val watcher = new Watcher {
      override def process(event: WatchedEvent): Unit = {
        logger.info("Run consumer watcher process\nPATH: " + "/"+Config.Keyspace)
        logger.info("Sleep" + Config.consumerTimeout + "secs")
        Thread.sleep(Config.consumerTimeout*1000)
        logger.info("Check transaction")
        var transactionsCount = receivedTransactions.length
        var checkTimestamp:Long = 0
        for (transaction <- receivedTransactions) {
          if (transaction.getTxnUUID.timestamp < checkTimestamp){
            transactionsCount = -1
          }
          checkTimestamp = transaction.getTxnUUID.timestamp
          logger.info("Received: " + transaction.getTxnUUID)
        }
        logger.info("Write consumer report")
//        val data = zkService.get[Int]("/"+Config.Keyspace)
        val instancePath = "/"+Config.Keyspace+"/"+instance.name.toString
        if (zkService.exist(instancePath)) zkService.setData(instancePath,transactionsCount)
        else zkService.create(instancePath, transactionsCount, org.apache.zookeeper.CreateMode.PERSISTENT)
        for (transaction <- receivedTransactions) {
          zkService.create(instancePath+"/"+transaction.getTxnUUID, "",org.apache.zookeeper.CreateMode.PERSISTENT)
        }
        logger.info("Waiting for stop consumer.")
        System.exit(0)
      }
    }

    zkService.setWatcher("/"+Config.Keyspace, watcher)
    while (true) {
      val consumedTxn: Option[BasicConsumerTransaction[Array[Byte], String]] = instance.getTransaction
      if (consumedTxn.isDefined) {
          receivedTransactions += consumedTxn.get
        logger.info(s"TRANSACTION: ${consumedTxn.get.getTxnUUID}")
      } else {
        logger.info("No transaction")
      }
      Thread.sleep((Config.consumerCheckInterval*1000).toInt)
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
