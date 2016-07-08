package com.ib.agent

import scala.util.Properties.envOrElse
import org.apache.log4j.Logger
import com.ib.agent.instance.Runner

object Main extends App {
  val logger = Logger.getLogger(getClass)
  val agentType = envOrElse("TYPE", "0").toInt


  def wrongType() = throw new IllegalArgumentException("Instance type is wrong")


  agentType match {
    case 0 => Runner.runProducer()
    case 1 => Runner.runMaster()
    case 2 => Runner.runConsumer()
    case _ => wrongType()
  }
}