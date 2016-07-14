package com.ib.framework

/**
  * Created by diryavkin_dn on 24.06.16.
  */

import com.mongodb.casbah.Imports._

object MongoFactory {
//  private val SERVER = "192.168.1.225"
//  private val PORT   = 27017
//  private val DATABASE = "testdb"
//  private val COLLECTION = "stocks"
  val connection = getConnection
  val collection = connection(Config.mongoDB)(Config.mongoCollection)


  def getConnection: MongoConnection = {
    MongoConnection(Config.mongoHost)
  }

  def getDatabase(conn: MongoConnection): MongoDB = {
    conn(Config.mongoDB)
  }

  def getCollection(conn: MongoConnection): MongoCollection = {
    conn(Config.mongoDB)(Config.mongoCollection)
  }

  def closeConnection(conn: MongoConnection) {
    conn.close
  }

}