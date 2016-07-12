package com.ib.framework

import java.util
import com.bwsw.tstreams.common.zkservice.ZkService
import org.apache.mesos.Protos._
import org.apache.mesos.{Scheduler, SchedulerDriver}
import org.apache.log4j.Logger
import org.json4s.native.JsonMethods._
import collection.mutable
import scala.collection.JavaConverters._


class TstreamsScheduler extends Scheduler {
//  var ports = collection.mutable.ListBuffer(31000 to 32000)
  private val logger = Logger.getLogger(getClass)

  def error(driver: SchedulerDriver, message: String) {
    logger.error(s"Got error message: $message")
  }

  def executorLost(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, status: Int) {}

  def slaveLost(driver: SchedulerDriver, slaveId: SlaveID) {}

  def disconnected(driver: SchedulerDriver) {}

  def frameworkMessage(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, data: Array[Byte]) {
    logger.info(s"Got framework message")
    logger.debug(s"$data")
  }

  def statusUpdate(driver: SchedulerDriver, status: TaskStatus) {
    logger.info(s"STATUS UPDATE:")
    TaskController.statusHandler(status)
  }

  def offerRescinded(driver: SchedulerDriver, offerId: OfferID) {
  }

  override def resourceOffers(driver: SchedulerDriver, offers: util.List[Offer]) {
    logger.info(s"RESOURCE OFFERS ")
    logger.info(s"${TaskController.getCommandsToLaunch}")
//    logger.debug("OFFERS: "+offers)

    // TODO: REMOVE THIS CODE
    var currentOffer: Offer = null
    for (offer <- offers.asScala) {if (offer.getAttributesCount == 0) currentOffer = offer}
    if (currentOffer==null) {for (offer <- offers.asScala) driver.declineOffer(offer.getId); return}

//    var currentOffer = offers.asScala.

    val cpus = Resource.newBuilder.setType(org.apache.mesos.Protos.Value.Type.SCALAR).setName("cpus").
      setScalar(org.apache.mesos.Protos.Value.Scalar.newBuilder.setValue(Config.cpuPerTask)).build
    val mem = Resource.newBuilder.setType(org.apache.mesos.Protos.Value.Type.SCALAR).setName("mem").
      setScalar(org.apache.mesos.Protos.Value.Scalar.newBuilder.setValue(Config.memPerTask)).build
    val disk = Resource.newBuilder.setType(org.apache.mesos.Protos.Value.Type.SCALAR).setName("disk").
      setScalar(org.apache.mesos.Protos.Value.Scalar.newBuilder.setValue(Config.diskPerTask)).build
    logger.debug(s"RESOURCES PER TASK: Cores: ${Config.cpuPerTask}, Mem: ${Config.memPerTask}, Disk: ${Config.diskPerTask};")
    logger.debug(s"TASKS COUNT: "+TaskController.commands.size)

//    var offerNumber = 0

    var launchedTasks: mutable.Map[OfferID, mutable.ListBuffer[TaskInfo]] = mutable.Map()
    for (command <- TaskController.getCommandsToLaunch) {
      logger.debug(s"Create task $command")

//      val currentOffer = getNextOffer(offers.asScala, offerNumber)

//      val currentOffer = offers.asScala(offerNumber)
//      if (offerNumber >= offers.asScala.size - 1) {
//        offerNumber = 0
//      } else {
//        offerNumber += 1
//      }

      // Create new task to launch
      val task = TaskInfo.newBuilder.setName(command).setTaskId(TaskID.newBuilder.setValue(command)).
        addResources(cpus).addResources(mem).addResources(disk).
        setCommand(TaskController.commands(command)).setSlaveId(currentOffer.getSlaveId).build

      val listTask = mutable.ListBuffer(task)
      if (launchedTasks.contains(currentOffer.getId)) {
        launchedTasks += (currentOffer.getId -> (launchedTasks(currentOffer.getId) ++ listTask))
      } else {launchedTasks += (currentOffer.getId -> listTask)}
//      TaskController.addLaunched(task.getTaskId.getValue)
//      TaskController.tasksToLaunch -= task.getTaskId.getValue

    }

    for (task <- launchedTasks) {
      driver.launchTasks(List(task._1).asJava, task._2.asJava)

    }

    for (offer <- offers.asScala) {
      driver.declineOffer(offer.getId)
    }

  }


  def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo) {
    logger.info(s"New master $masterInfo")
  }


  def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo) {
    val zkService = new ZkService(Config.rootPath, List(new java.net.InetSocketAddress(Config.zkHost, Config.zkPort)), 60)
    if (zkService.exist(Config.benchmarkPath)) zkService.deleteRecursive(Config.benchmarkPath)
    logger.info(s"Registered framework as: ${frameworkId.getValue}")
    TaskController.setDriver(driver)
    val urlParams = scala.util.Properties.envOrElse("PARAMS_URL", "http://192.168.1.225:8000/params.json")
    val benchmarksParams = parse(scala.io.Source.fromURL(urlParams).mkString)
    TaskController.initialize(benchmarksParams\"benchmarks", getJar)
  }

  def getJar:String = {
    scala.util.Properties.envOrElse("JAR", "http://192.168.1.225:8000/ib-agent.jar")
  }

//  def addLaunchedTask(taskId:String) = {
//    taskId.split("_").last match {
//      case "producer" => TaskController
//      case "consumer" => consumerHandler(status)
//      case "master" => masterHandler(status)
//    }
//  }
}
