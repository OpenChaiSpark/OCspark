package com.pointr.spark.rdd

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import com.pointr.tcp.rpc.{P2pConnectionParams, ServerIfConf, SolverServerIf}
import com.pointr.tcp.rpc.TcpServer.DefaultConfPath
import com.pointr.tcp.util.YamlUtils

import scala.reflect.ClassTag

object SolverRDD {
  val weightsMergePolicy: String = "best"
}

class SolverRDD[KVO:ClassTag,T:ClassTag](sc: SparkContext, parent: RDD[KVO], p2pParams: P2pConnectionParams)
  extends P2pRDD[KVO,T](sc, parent, p2pParams, new SolverServerIf(YamlUtils.toScala[ServerIfConf](DefaultConfPath))) {
}
