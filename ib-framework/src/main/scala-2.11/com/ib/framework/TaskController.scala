package com.ib.framework

/**
  * Created by diryavkin_dn on 04.06.16.
  */

import com.datastax.driver.core.Cluster
import org.apache.log4j.Logger
import org.apache.mesos.Protos._
import org.apache.mesos.SchedulerDriver
import org.json4s.JsonAST.JValue
import collection.mutable


object TaskController {
  private val logger = Logger.getLogger(getClass)

  var jar: String = null
  var driver: SchedulerDriver = null
  val tasksToLaunch: mutable.ListBuffer[String] = mutable.ListBuffer()
  val producerTasks: mutable.ListBuffer[String] = mutable.ListBuffer()
  val masterTasks: mutable.ListBuffer[String] = mutable.ListBuffer()
  val consumerTasks: mutable.ListBuffer[String] = mutable.ListBuffer()

  var currentBenchmark: Benchmark = null
  val benchmarksQueue: mutable.Queue[String] = mutable.Queue()
  val commands: mutable.Map[String, CommandInfo.Builder] = mutable.Map()



  def setDriver(driver: SchedulerDriver) = {
    /**
      * Save driver
      */
    this.driver = driver
  }

  def initialize(params:JValue, urlJar:String): Unit = {
    /**
      *
      */
    jar = urlJar
    for (bench <- params.children){
      if (BenchmarkList.newBenchmark(bench)) {
        val benchmark = BenchmarkList.getByName((bench\"name").values.toString)
        for (x <- 1 to benchmark.count) {
          this.addBenchmark(benchmark.name)
        }
      } else {
        logger.info("Not correct parameters")
      }
    }
    nextBenchmark()
  }

  var cnt = 0
  def nextBenchmark() = {
    Config.newKeyspace(cnt)
    cnt += 1
    val cluster = Cluster.builder().addContactPoint("192.168.1.225").build()
    val session = cluster.connect()
    CassandraHelper.createKeyspace(session, Config.keyspace)
    CassandraHelper.createMetadataTables(session, Config.keyspace)
    CassandraHelper.createDataTable(session, Config.keyspace)
    session.close()
    currentBenchmark = BenchmarkList.getByName(this.getBenchmark)
    val commands = currentBenchmark.prepareCommands(jar)
    for (command <- commands) {
      tasksToLaunch+=command._1
      this.commands += command
    }
  }

  def getCommandsToLaunch:mutable.ListBuffer[String] = {
    tasksToLaunch
  }

  def getCurrentBenchmark:Benchmark = currentBenchmark

  def addBenchmark(benchmark:String) = benchmarksQueue += benchmark

  def getBenchmark:String = {
    if (benchmarksQueue.isEmpty) System.exit(0)
    benchmarksQueue.dequeue()
  }

  def statusHandler(status: TaskStatus) = {
    logger.info(status.getTaskId.getValue)
    logger.info(status.getState)
    logger.info(status.getMessage)
    val taskId = status.getTaskId.getValue
    taskId.split("_").last match {
      case "producer" => producerHandler(status)
      case "consumer" => consumerHandler(status)
      case "master" => masterHandler(status)
    }
  }

  def producerHandler(status: TaskStatus) = {
    status.getState.toString match {
      case "TASK_RUNNING" =>
        tasksToLaunch -= status.getTaskId.getValue
        producerTasks+=status.getTaskId.getValue
        logger.info(s"producer tasks: $producerTasks")

      case "TASK_FINISHED" =>
        if (producerTasks.contains(status.getTaskId.getValue)) {
          producerTasks -= status.getTaskId.getValue
          if (producerTasks.isEmpty && tasksToLaunch.isEmpty) {
//            finishBenchmark()
            afterProducersFinished()
          }
        }

      case "TASK_ERROR" =>
        tasksToLaunch -= status.getTaskId.getValue

      case "TASK_FAILED" =>
        logger.error("FAILED STATUS: " + status)
        exceptionalFinish("message")

      case _ =>
    }
  }


  def masterHandler(status: TaskStatus) = {
    status.getState.toString match {
      case "TASK_RUNNING" =>
        tasksToLaunch -= status.getTaskId.getValue
        masterTasks+=status.getTaskId.getValue
        logger.debug(s"master tasks: $masterTasks")

      case "TASK_FINISHED" =>

      case "TASK_FAILED" =>
        val thread = new Thread {
          override def run(): Unit = {
            val r = scala.util.Random
            val high = getCurrentBenchmark.masterRestart.last
            val low = getCurrentBenchmark.masterRestart.head
            val sleepTime = r.nextInt(high-low)+low
            Thread.sleep(sleepTime*1000)
            tasksToLaunch+=status.getTaskId.getValue
          }
        }
        thread.start()
        masterTasks-=status.getTaskId.getValue



      case "TASK_ERROR" =>
//        tasksToLaunch -= status.getTaskId.getValue

      case _ =>
        masterTasks-=status.getTaskId.getValue
    }
  }

  def consumerHandler(status: TaskStatus) = {
    status.getState.toString match {
      case "TASK_RUNNING" =>
        tasksToLaunch -= status.getTaskId.getValue
        consumerTasks+=status.getTaskId.getValue
        logger.debug(s"consumer tasks: $consumerTasks")

      case "TASK_FINISHED" =>
//        logger.info(s"CONSUMER FINISHED: ${status.getTaskId.getValue}")
        if (consumerTasks.contains(status.getTaskId.getValue)) {
          consumerTasks -= status.getTaskId.getValue
          if (consumerTasks.isEmpty) {
            finishBenchmark("message")
          }
        }

      case "TASK_ERROR" =>
//        tasksToLaunch -= status.getTaskId.getValue

      case "TASK_FAILED" =>
        logger.error("FAILED STATUS: " + status)
//        logger.info(s"CONSUMER FAILED: ${status.getTaskId.getValue}")
        exceptionalFinish("message")

      case _ =>
    }
  }

  def afterProducersFinished() = {
    var overCount = 0
    val paths = Services.zkService.getAllSubPath("/"+Config.keyspace).get
    for (path <- paths) {
      val count = Services.zkService.get[Int]("/"+Config.keyspace+"/"+path)
      overCount+=count.get
    }
    logger.debug("NOTIFY PATH: " + "/"+Config.keyspace)
    Services.zkSetData("/"+Config.keyspace+"/total", overCount)
    Services.zkSetData("/"+Config.keyspace, overCount)
  }

  def exceptionalFinish(message:String) = {
    Services.zkSetData("/"+Config.keyspace+"/total", -1)
    for (task <- masterTasks){
      driver.killTask(TaskID.newBuilder.setValue(task).build)
      masterTasks -= task
    }
    for (command <- tasksToLaunch) {
      tasksToLaunch -= command
    }
    logger.info("Benchmark exceptional finished")
    nextBenchmark()
  }

  def finishBenchmark(message:String)={
    val report: collection.mutable.Map[String, Int] = collection.mutable.Map()
//    val overCount = zkService.get[Int]("/"+keyspace+"/total").get

    val paths = Services.zkService.getAllSubPath("/"+Config.keyspace).get
    for (path <- paths) {
      if (path.split("_").last == "consumer") {
        val pathData = Services.zkService.get[Int]("/"+Config.keyspace+"/"+path).get
        report += path -> pathData
      }
    }
    Services.zkService.setData("/"+Config.keyspace, report)

//    val cluster = Cluster.builder().addContactPoint("192.168.1.225").build()
//    val session = cluster.connect()
//    session.close()

    for (task <- masterTasks){
      driver.killTask(TaskID.newBuilder.setValue(task).build)
      masterTasks -= task
    }
    for (command <- tasksToLaunch) {
      tasksToLaunch -= command
    }
    logger.info("Benchmark finished")
    nextBenchmark()
  }

  def addLaunched(taskId:String) = {
    taskId.split("_").last match {
      case "producer" => producerTasks += taskId
      case "consumer" => consumerTasks += taskId
      case "master" => masterTasks += taskId
    }
  }

}
