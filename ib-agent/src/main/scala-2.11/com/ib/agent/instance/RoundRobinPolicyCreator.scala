package com.ib.agent.instance

import com.bwsw.tstreams.policy.RoundRobinPolicy
import com.bwsw.tstreams.streams.BasicStream

/**
  * Created by diryavkin_dn on 27.05.16.
  */
object RoundRobinPolicyCreator {
  /**
    *
    * @param stream Stream instance
    * @param usedPartitions Policy partitions to use
    * @return RoundRobinPolicy instance
    */
  def getRoundRobinPolicy(stream : BasicStream[_], usedPartitions : List[Int]) : RoundRobinPolicy =
    new RoundRobinPolicy(stream, usedPartitions)
}
