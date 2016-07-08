package com.ib.agent.instance

import com.bwsw.tstreams.generator.LocalTimeUUIDGenerator

/**
  * Created by diryavkin_dn on 27.05.16.
  */
object LocalGeneratorCreator {
  def getGen = new LocalTimeUUIDGenerator
}
