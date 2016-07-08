package com.ib.framework

/**
  * Created by diryavkin_dn on 31.05.16.
  */

import org.json4s._


object BenchmarkList {
  var currentBenchmark:Benchmark = null

  val benchmarkList: collection.mutable.Map[String, Benchmark] = collection.mutable.Map()

  def newBenchmark(params:JValue):Boolean = {
    val bench = new Benchmark(params)
    val valid = bench.validate()
    if (valid) benchmarkList += bench.name -> bench
    valid
  }

  def getByName(name:String): Benchmark = {
    if (benchmarkList.isEmpty) System.exit(0)
    benchmarkList(name)
  }

}
