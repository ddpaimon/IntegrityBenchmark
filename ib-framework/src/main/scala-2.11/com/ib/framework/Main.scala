package com.ib.framework

import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos.FrameworkInfo

import scala.util.Properties


object Main extends App {
  val framework = FrameworkInfo.newBuilder.
    setName("TstreamsFramework").
    setUser("root").
    setRole("*").
    setCheckpoint(false).
    setFailoverTimeout(0.0d).
    build()

  val scheduler = new TstreamsScheduler
  val master_path = Properties.envOrElse("MESOS_MASTER", "zk://127.0.0.1:2181/mesos")
  val driver = new MesosSchedulerDriver(scheduler, framework, master_path)

  driver.start()
  driver.join()
}