package com.ib.framework

/**
  * Created by diryavkin_dn on 24.06.16.
  */

import com.mongodb.casbah.Imports._

object MongoFactory {
  private val SERVER = "192.168.1.225"
  private val PORT   = 27017
  private val DATABASE = "testdb"
  private val COLLECTION = "stocks"
  val connection = MongoConnection(SERVER)
  val collection = connection(DATABASE)(COLLECTION)


  def getConnection: MongoConnection = {
    MongoConnection(SERVER)
  }

  def getDatabase(conn: MongoConnection): MongoDB = {
    conn(DATABASE)
  }

  def getCollection(conn: MongoConnection): MongoCollection = {
    conn(DATABASE)(COLLECTION)
  }

  def closeConnection(conn: MongoConnection) {
    conn.close
  }

}