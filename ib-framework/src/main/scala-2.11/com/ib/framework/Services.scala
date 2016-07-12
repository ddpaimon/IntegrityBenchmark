package com.ib.framework

/**
  * Created by diryavkin_dn on 01.07.16.
  */
import com.bwsw.tstreams.common.zkservice.ZkService

object Services {
  val zkService = new ZkService(Config.rootPath+Config.benchmarkPath,
    List(new java.net.InetSocketAddress(Config.zkHost, Config.zkPort)), 60)

  def zkSetData(path:String, data:Any): Unit = {
    if (zkService.exist(path)) zkService.setData(path, data)
    else zkService.create(path, data, org.apache.zookeeper.CreateMode.PERSISTENT)
  }
}
