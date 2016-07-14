package com.ib.framework

/**
  * Created by diryavkin_dn on 24.06.16.
  */

import com.mongodb.casbah.Imports._

class Report(builder: ReportBuilder) {
  val result = builder.result
  val keyspace = builder.keyspace
  val benchmarkName = builder.benchmarkName
  val number = builder.number
  val totalTransactionsCount = builder.totalTransactionsCount
  val consumersTransactions = builder.consumersTransactions
  val message = builder.message

  def save = {
    val mongoObj = buildMongoDbObject
    MongoFactory.getCollection(MongoFactory.getConnection).save(mongoObj)
  }

  def buildMongoDbObject: MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "_id" -> ("report_" + Report.cnt)
    builder += "result" -> result
    builder += "keyspace" -> keyspace
    builder += "benchmark-name" -> benchmarkName
    builder += "number" -> number
    builder += "total-transactions-count" ->  totalTransactionsCount
    builder += "consumersTransactions" -> consumersTransactions
    builder += "message" -> message
    builder.result
  }
}


object Report {
  var number = 0
  def newBuilder: ReportBuilder = new ReportBuilder()
  def cnt: Int = {
    number += 1
    number
  }
}


class ReportBuilder{
  var result: Boolean = true
  var keyspace: String = "keyspace"
  var benchmarkName: String = "benchmark"
  var number: Int = 1
  var totalTransactionsCount: Int = 0
  val consumersTransactions: collection.mutable.Map[String, Int] = collection.mutable.Map()
  var message: String = ""

  def setResult(result:Boolean): ReportBuilder = {
    this.result = result
    this
  }

  def setKeyspace(keyspace:String): ReportBuilder = {
    this.keyspace = keyspace
    this
  }

  def setBenchmarkName(benchmarkName:String): ReportBuilder = {
    this.benchmarkName = benchmarkName
    this
  }

  def setNumber(number:Int): ReportBuilder = {
    this.number = number
    this
  }

  def setTotalTransactionsCount(totalTransactionsCount:Int): ReportBuilder = {
    this.totalTransactionsCount = totalTransactionsCount
    this
  }

  def setConsumersTransactions(consumerId:String, count:Int): ReportBuilder = {
    this.consumersTransactions += consumerId -> count
    this
  }

  def setMessage(message:String): ReportBuilder = {
    this.message = message
    this
  }

  def build: Report = new Report(this)
}





