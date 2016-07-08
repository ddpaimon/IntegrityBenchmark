package com.ib.framework

/**
  * Created by diryavkin_dn on 24.06.16.
  */

import com.mongodb.casbah.Imports._

class Report(builder: ReportBuilder) {
  var result = builder.result

  def save = {
    val mongoObj = buildMongoDbObject
    MongoFactory.getCollection(MongoFactory.getConnection).save(mongoObj)
  }

  def buildMongoDbObject: MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "result" -> this.result
    builder += "_id" -> "report"
    builder.result
  }
}


object Report {
  def newBuilder: ReportBuilder = new ReportBuilder()
}


class ReportBuilder{
  var result: Boolean = true

  def setResult(result:Boolean): ReportBuilder = {
    this.result = result
    this
  }

  def build: Report = new Report(this)
}





