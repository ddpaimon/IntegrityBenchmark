package com.ib.agent.instance

/**
  * Created by diryavkin_dn on 27.05.16.
  */

import java.net.InetSocketAddress

import com.bwsw.tstreams.agents.consumer.{BasicConsumer, BasicConsumerOptions}
import com.bwsw.tstreams.agents.consumer.Offsets.Oldest
import com.bwsw.tstreams.agents.producer.{BasicProducer, BasicProducerOptions, ProducerCoordinationOptions}
import com.bwsw.tstreams.converter.ArrayByteToStringConverter
import com.bwsw.tstreams.agents.producer.InsertionType.BatchInsert
import com.bwsw.tstreams.converter.StringToArrayByteConverter
import com.bwsw.tstreams.coordination.transactions.transport.impl.TcpTransport
import com.bwsw.tstreams.data.cassandra.{CassandraStorageFactory, CassandraStorageOptions}
import com.bwsw.tstreams.metadata.MetadataStorageFactory
import com.bwsw.tstreams.streams.BasicStream
import org.apache.log4j.Logger

object Instance {

  val logger = Logger.getLogger(getClass)

  protected val batchSizeVal = 5

  //metadata/data factories
  private val metadataStorageFactory = new MetadataStorageFactory
  val storageFactory = new CassandraStorageFactory

  //converters to convert usertype->storagetype; storagetype->usertype
  val arrayByteToStringConverter = new ArrayByteToStringConverter
  val stringToArrayByteConverter = new StringToArrayByteConverter

  //cassandra storage instances
  val cassandraStorageOptions = new CassandraStorageOptions(List(new InetSocketAddress(Config.cassandraHost,Config.cassandraPort)), Config.Keyspace)
  val cassandraInstance = storageFactory.getInstance(cassandraStorageOptions)

  //metadata storage instances
  val metadataStorageInstance = metadataStorageFactory.getInstance(
    cassandraHosts = List(new java.net.InetSocketAddress(Config.cassandraHost,Config.cassandraPort)),
    keyspace = Config.Keyspace)

  val streamForInstance = new BasicStream[Array[Byte]](
    name = "instance_stream",
    partitions = 3,
    metadataStorage = metadataStorageInstance,
    dataStorage = cassandraInstance,
    ttl = 60 * 10,
    description = "some_description")

  def getProducer: BasicProducer[String, Array[Byte]] = {
    logger.info(s"Run Producer on ${Config.address}")
//    logger.info(s"ZOOKEEPER::::   " + List(new InetSocketAddress(Config.zookeeperHost, Config.zookeeperPort)) + "PATH" +Config.rootPath)
    val agentSettings = new ProducerCoordinationOptions(
      agentAddress = Config.address,
      zkHosts = List(new java.net.InetSocketAddress(Config.zookeeperHost, Config.zookeeperPort)),
      zkRootPath = Config.rootPath,
      zkTimeout = 10000,
      isLowPriorityToBeMaster = true,
      transport = new TcpTransport,
      transportTimeout = Config.transportTimeout)

    val options = new BasicProducerOptions[String, Array[Byte]](
      transactionTTL = Config.TTL,
      transactionKeepAliveInterval = Config.transactionKeepAliveInterval,
      producerKeepAliveInterval = Config.transactionKeepAliveInterval,
      RoundRobinPolicyCreator.getRoundRobinPolicy(streamForInstance, List(0,1,2)),
      BatchInsert(batchSizeVal),
      LocalGeneratorCreator.getGen,
      agentSettings,
      stringToArrayByteConverter)
    logger.info("zk host: " + Config.zookeeperHost)
    logger.info("zk hosts: " +  List(new java.net.InetSocketAddress(Config.zookeeperHost, Config.zookeeperPort)))
    logger.info("zk root path: " + Config.rootPath)
    new BasicProducer(Config.taskId, streamForInstance, options)
  }

  def getMaster: BasicProducer[String, Array[Byte]] = {
    logger.info(s"Run Master on ${Config.address}")
    val agentSettings = new ProducerCoordinationOptions(
      agentAddress = Config.address,
      zkHosts = List(new java.net.InetSocketAddress(Config.zookeeperHost, Config.zookeeperPort)),
      zkRootPath = Config.rootPath,
      zkTimeout = 10000,
      isLowPriorityToBeMaster = false,
      transport = new TcpTransport,
      transportTimeout = 5)

    val options = new BasicProducerOptions[String, Array[Byte]](
      transactionTTL = Config.TTL,
      transactionKeepAliveInterval = Config.transactionKeepAliveInterval,
      producerKeepAliveInterval = Config.transactionKeepAliveInterval,
      RoundRobinPolicyCreator.getRoundRobinPolicy(streamForInstance, List(0,1,2)),
      BatchInsert(batchSizeVal),
      LocalGeneratorCreator.getGen,
      agentSettings,
      stringToArrayByteConverter)
    new BasicProducer(Config.taskId, streamForInstance, options)
  }

  def getConsumer: BasicConsumer[Array[Byte], String] = {
    logger.info(s"Run Consumer on ${Config.address}")
    val options = new BasicConsumerOptions[Array[Byte], String](
      transactionsPreload = Config.transactionPreload,
      dataPreload = Config.dataPreload,
      consumerKeepAliveInterval = Config.consumerKeepAliveInterval,
      arrayByteToStringConverter,
      RoundRobinPolicyCreator.getRoundRobinPolicy(streamForInstance, List(0,1,2)),
      Oldest,
      LocalGeneratorCreator.getGen,
      useLastOffset = Config.useLastOffset)
    new BasicConsumer(Config.taskId, streamForInstance, options)
  }

}
